package com.xtopdf.xtopdf.adapters.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Docker implementation of the ContainerRuntimePort.
 * This adapter uses Docker Java API to manage container lifecycle.
 * 
 * To switch to another runtime (like Podman), create a new adapter
 * implementing ContainerRuntimePort (e.g., PodmanContainerAdapter).
 */
@Slf4j
public class DockerContainerAdapter implements ContainerRuntimePort {
    
    // Port range configuration for fallback when dynamic port allocation fails
    private static final int PORT_RANGE_START = 30000;
    private static final int PORT_RANGE_SIZE = 10000;
    
    private final ContainerConfig config;
    private final DockerClient dockerClient;
    private final RestTemplate restTemplate;
    private final boolean enabled;
    
    public DockerContainerAdapter(ContainerConfig config, boolean enabled) {
        this.config = config;
        this.enabled = enabled;
        this.restTemplate = new RestTemplate();
        
        if (enabled) {
            // Initialize Docker client only when orchestration is enabled
            var dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            var httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(dockerConfig.getDockerHost())
                    .sslConfig(dockerConfig.getSSLConfig())
                    .build();
            this.dockerClient = DockerClientImpl.getInstance(dockerConfig, httpClient);
            log.info("Docker container adapter initialized with image: {}", config.getImageName());
        } else {
            this.dockerClient = null;
            log.info("Docker container adapter disabled");
        }
    }
    
    @Override
    public void executeInContainer(MultipartFile inputFile, String outputFile, 
                                   Runnable converterLogic) throws FileConversionException {
        if (!enabled) {
            // If container orchestration is disabled, execute locally
            converterLogic.run();
            return;
        }
        
        String containerId = null;
        int hostPort = -1;
        
        try {
            // Create and start container
            hostPort = findAvailablePort();
            containerId = createAndStartContainer(hostPort);
            
            // Wait for container to be ready
            String containerUrl = "http://localhost:" + hostPort;
            waitForContainerReady(containerUrl, containerId);
            
            // Execute conversion via container's API
            executeConversionInContainer(containerUrl, inputFile, outputFile);
            
            log.info("Successfully completed conversion in Docker container {}", containerId);
            
        } catch (Exception e) {
            throw new FileConversionException("Failed to execute conversion in Docker container: " + 
                    e.getMessage(), e);
        } finally {
            // Cleanup container
            if (containerId != null && config.isCleanupEnabled()) {
                cleanupContainer(containerId);
            }
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String getRuntimeInfo() {
        if (dockerClient == null) {
            return "Docker adapter not initialized (orchestration disabled)";
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
    
    /**
     * Create and start a Docker container for conversion
     */
    private String createAndStartContainer(int hostPort) {
        // Expose port from container
        ExposedPort containerPort = ExposedPort.tcp(config.getContainerPort());
        Ports portBindings = new Ports();
        portBindings.bind(containerPort, Ports.Binding.bindPort(hostPort));
        
        // Create host config with resource limits
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withMemory(parseMemoryLimit(config.getMemoryLimit()))
                .withNanoCPUs((long) config.getCpuLimit() * 1_000_000_000L)
                .withAutoRemove(false); // We'll remove manually
        
        // Create container
        CreateContainerResponse container = dockerClient.createContainerCmd(config.getImageName())
                .withHostConfig(hostConfig)
                .withExposedPorts(containerPort)
                .exec();
        
        String containerId = container.getId();
        log.info("Created Docker container {} for conversion job", containerId);
        
        // Start container
        dockerClient.startContainerCmd(containerId).exec();
        log.info("Started Docker container {} on port {}", containerId, hostPort);
        
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
                    log.info("Docker container {} is ready", containerId);
                    return;
                }
            } catch (Exception e) {
                log.debug("Waiting for Docker container {} to be ready (attempt {}/{})", 
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
                "Docker container failed to become ready within timeout period");
    }
    
    /**
     * Execute the conversion via container's REST API.
     * The container must expose the same /api/convert endpoint as the main application,
     * accepting multipart form data with "inputFile" and "outputFile" parameters.
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
        
        // Extract just the filename for the container's output path
        File outputFileObj = new File(outputFile);
        String outputFileName = outputFileObj.getName();
        
        body.add("inputFile", fileResource);
        body.add("outputFile", outputFileName);
        
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
        
        log.info("Conversion completed successfully in Docker container");
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
                log.debug("Stopped Docker container {}", containerId);
            } catch (Exception e) {
                log.debug("Docker container {} was already stopped", containerId);
            }
            
            // Remove container
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .exec();
            log.info("Removed Docker container {}", containerId);
            
        } catch (Exception e) {
            log.warn("Failed to cleanup Docker container {}: {}", containerId, e.getMessage());
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
            return PORT_RANGE_START + (int) (Math.random() * PORT_RANGE_SIZE);
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
}
