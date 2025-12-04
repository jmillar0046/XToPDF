package com.xtopdf.xtopdf.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.xtopdf.xtopdf.config.ContainerOrchestrationConfig;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Service for orchestrating Docker containers for file conversion jobs.
 * Each conversion runs in an isolated container that is created on-demand
 * and cleaned up after the job completes.
 */
@Service
@Slf4j
public class ContainerOrchestrationService {
    
    private final ContainerOrchestrationConfig config;
    private final DockerClient dockerClient;
    private final RestTemplate restTemplate;
    
    public ContainerOrchestrationService(ContainerOrchestrationConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        
        if (config.isEnabled()) {
            // Initialize Docker client only when orchestration is enabled
            var dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            var httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(dockerConfig.getDockerHost())
                    .sslConfig(dockerConfig.getSSLConfig())
                    .build();
            this.dockerClient = DockerClientImpl.getInstance(dockerConfig, httpClient);
            log.info("Container orchestration enabled with image: {}", config.getImage().getFullName());
        } else {
            this.dockerClient = null;
            log.info("Container orchestration disabled");
        }
    }
    
    /**
     * Check if container orchestration is enabled
     */
    public boolean isEnabled() {
        return config.isEnabled();
    }
    
    /**
     * Execute a conversion job in an isolated container.
     * 
     * @param inputFile The input file to convert
     * @param outputFile The desired output file path
     * @param converterLogic The conversion logic to execute in local mode (when orchestration disabled)
     * @throws FileConversionException if the conversion fails
     */
    public void executeInContainer(MultipartFile inputFile, String outputFile, 
                                     Runnable converterLogic) throws FileConversionException {
        if (!config.isEnabled()) {
            // If container orchestration is disabled, execute locally
            converterLogic.run();
            return;
        }
        
        String containerId = null;
        int containerPort = -1;
        
        try {
            // Create and start container
            containerPort = findAvailablePort();
            containerId = createAndStartContainer(containerPort);
            
            // Wait for container to be ready
            String containerUrl = "http://localhost:" + containerPort;
            waitForContainerReady(containerUrl, containerId);
            
            // Execute conversion via container's API
            executeConversionInContainer(containerUrl, inputFile, outputFile);
            
            log.info("Successfully completed conversion in container {}", containerId);
            
        } catch (Exception e) {
            throw new FileConversionException("Failed to execute conversion in container: " + 
                    e.getMessage(), e);
        } finally {
            // Cleanup container
            if (containerId != null && config.isCleanupEnabled()) {
                cleanupContainer(containerId);
            }
        }
    }
    
    /**
     * Create and start a Docker container for conversion
     */
    private String createAndStartContainer(int hostPort) {
        // Expose port 8080 from container
        ExposedPort tcp8080 = ExposedPort.tcp(8080);
        Ports portBindings = new Ports();
        portBindings.bind(tcp8080, Ports.Binding.bindPort(hostPort));
        
        // Create host config with resource limits
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withMemory(parseMemoryLimit(config.getMemoryLimit()))
                .withNanoCPUs((long) config.getCpuLimit() * 1_000_000_000L)
                .withAutoRemove(false); // We'll remove manually
        
        // Create container
        CreateContainerResponse container = dockerClient.createContainerCmd(config.getImage().getFullName())
                .withHostConfig(hostConfig)
                .withExposedPorts(tcp8080)
                .exec();
        
        String containerId = container.getId();
        log.info("Created container {} for conversion job", containerId);
        
        // Start container
        dockerClient.startContainerCmd(containerId).exec();
        log.info("Started container {} on port {}", containerId, hostPort);
        
        return containerId;
    }
    
    /**
     * Wait for container to be ready to accept requests
     */
    private void waitForContainerReady(String containerUrl, String containerId) 
            throws FileConversionException {
        int maxAttempts = 30;
        int attemptDelay = 1000; // 1 second
        
        for (int i = 0; i < maxAttempts; i++) {
            try {
                // Try to connect to the container (simple ping to root)
                ResponseEntity<String> response = restTemplate.getForEntity(
                        containerUrl, String.class);
                
                if (response.getStatusCode().is2xxSuccessful() || 
                    response.getStatusCode().is4xxClientError()) {
                    // Container is responding (even if endpoint doesn't exist, it's up)
                    log.info("Container {} is ready", containerId);
                    return;
                }
            } catch (Exception e) {
                log.debug("Waiting for container {} to be ready (attempt {}/{})", 
                        containerId, i + 1, maxAttempts);
            }
            
            try {
                Thread.sleep(attemptDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new FileConversionException("Interrupted while waiting for container", e);
            }
        }
        
        throw new FileConversionException(
                "Container failed to become ready within timeout period");
    }
    
    /**
     * Execute the conversion via container's REST API
     */
    private void executeConversionInContainer(String containerUrl, 
                                             MultipartFile inputFile, 
                                             String outputFile) throws IOException, FileConversionException {
        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Create a ByteArrayResource to properly send the file
        ByteArrayResource fileResource = new ByteArrayResource(inputFile.getBytes()) {
            @Override
            public String getFilename() {
                return inputFile.getOriginalFilename();
            }
        };
        
        body.add("inputFile", fileResource);
        body.add("outputFile", Paths.get(outputFile).getFileName().toString());
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
        
        // Call container's conversion endpoint
        ResponseEntity<String> response = restTemplate.postForEntity(
                containerUrl + "/api/convert",
                requestEntity,
                String.class
        );
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new FileConversionException(
                    "Container conversion failed with status: " + response.getStatusCode());
        }
        
        log.info("Conversion completed successfully in container");
    }
    
    /**
     * Cleanup container and remove it
     */
    private void cleanupContainer(String containerId) {
        try {
            // Stop container if still running
            try {
                dockerClient.stopContainerCmd(containerId)
                        .withTimeout(10)
                        .exec();
                log.debug("Stopped container {}", containerId);
            } catch (Exception e) {
                log.debug("Container {} was already stopped", containerId);
            }
            
            // Remove container
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();
            log.info("Removed container {}", containerId);
            
        } catch (Exception e) {
            log.warn("Failed to cleanup container {}: {}", containerId, e.getMessage());
        }
    }
    
    /**
     * Find an available port for the container
     */
    private int findAvailablePort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            // Fallback to random port in range
            return 30000 + (int) (Math.random() * 10000);
        }
    }
    
    /**
     * Parse memory limit string to bytes
     */
    private long parseMemoryLimit(String memoryLimit) {
        String lower = memoryLimit.toLowerCase();
        long multiplier = 1;
        
        if (lower.endsWith("k")) {
            multiplier = 1024L;
            lower = lower.substring(0, lower.length() - 1);
        } else if (lower.endsWith("m")) {
            multiplier = 1024L * 1024L;
            lower = lower.substring(0, lower.length() - 1);
        } else if (lower.endsWith("g")) {
            multiplier = 1024L * 1024L * 1024L;
            lower = lower.substring(0, lower.length() - 1);
        }
        
        try {
            return Long.parseLong(lower.trim()) * multiplier;
        } catch (NumberFormatException e) {
            log.warn("Invalid memory limit format: {}, using default 512MB", memoryLimit);
            return 512L * 1024L * 1024L;
        }
    }
    
    /**
     * Get Docker client info for diagnostics
     */
    public String getDockerInfo() {
        if (dockerClient == null) {
            return "Docker client not initialized (orchestration disabled)";
        }
        
        try {
            var info = dockerClient.infoCmd().exec();
            return String.format("Docker version: %s, Containers: %d, Images: %d",
                    info.getServerVersion(),
                    info.getContainers(),
                    info.getImages());
        } catch (Exception e) {
            return "Failed to get Docker info: " + e.getMessage();
        }
    }
}
