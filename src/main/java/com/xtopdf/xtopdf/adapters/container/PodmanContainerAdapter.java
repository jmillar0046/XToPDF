package com.xtopdf.xtopdf.adapters.container;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Podman implementation of the ContainerRuntimePort.
 * This adapter uses Podman CLI commands to manage container lifecycle.
 * 
 * Podman is a daemonless, rootless container engine that's compatible with Docker
 * but offers better security. It's a drop-in replacement for Docker.
 * 
 * To use this adapter instead of Docker, configure the bean in your Spring configuration:
 * @Bean
 * public ContainerRuntimePort containerRuntimePort(ContainerOrchestrationConfig config) {
 *     return new PodmanContainerAdapter(buildContainerConfig(config), config.isEnabled());
 * }
 */
@Slf4j
public class PodmanContainerAdapter implements ContainerRuntimePort {
    
    private final ContainerConfig config;
    private final RestTemplate restTemplate;
    private final boolean enabled;
    
    public PodmanContainerAdapter(ContainerConfig config, boolean enabled) {
        this.config = config;
        this.enabled = enabled;
        this.restTemplate = new RestTemplate();
        
        if (enabled) {
            // Verify Podman is installed
            try {
                Process process = Runtime.getRuntime().exec("podman --version");
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("Podman container adapter initialized with image: {}", config.getImageName());
                } else {
                    log.warn("Podman not found or not working properly");
                }
            } catch (Exception e) {
                log.error("Failed to verify Podman installation: {}", e.getMessage());
            }
        } else {
            log.info("Podman container adapter disabled");
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
            
            log.info("Successfully completed conversion in Podman container {}", containerId);
            
        } catch (Exception e) {
            throw new FileConversionException("Failed to execute conversion in Podman container: " + 
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
        try {
            Process process = Runtime.getRuntime().exec("podman version");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder info = new StringBuilder("Podman: ");
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    info.append(line);
                    first = false;
                    break;
                }
            }
            return info.toString();
        } catch (Exception e) {
            return "Failed to get Podman info: " + e.getMessage();
        }
    }
    
    /**
     * Create and start a Podman container for conversion
     */
    private String createAndStartContainer(int hostPort) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("podman");
        command.add("run");
        command.add("-d"); // Detached mode
        // Only auto-remove if cleanup is enabled
        if (config.isCleanupEnabled()) {
            command.add("--rm");
        }
        command.add("-p");
        command.add(hostPort + ":" + config.getContainerPort());
        command.add("--memory=" + config.getMemoryLimit());
        command.add("--cpus=" + config.getCpuLimit());
        command.add(config.getImageName());
        
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String containerId = reader.readLine();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String error = errorReader.lines().reduce("", (a, b) -> a + "\n" + b);
            throw new IOException("Failed to start Podman container: " + error);
        }
        
        log.info("Created Podman container {} for conversion job", containerId);
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
                ResponseEntity<String> response = restTemplate.getForEntity(
                        containerUrl, String.class);
                
                if (response.getStatusCode().is2xxSuccessful() || 
                    response.getStatusCode().is4xxClientError()) {
                    log.info("Podman container {} is ready", containerId);
                    return;
                }
            } catch (Exception e) {
                log.debug("Waiting for Podman container {} to be ready (attempt {}/{})", 
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
                "Podman container failed to become ready within timeout period");
    }
    
    /**
     * Execute the conversion via container's REST API
     */
    private void executeConversionInContainer(String containerUrl, 
                                             MultipartFile inputFile, 
                                             String outputFile) throws IOException, FileConversionException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        ByteArrayResource fileResource = new ByteArrayResource(inputFile.getBytes()) {
            @Override
            public String getFilename() {
                return inputFile.getOriginalFilename();
            }
        };
        
        File outputFileObj = new File(outputFile);
        String outputFileName = outputFileObj.getName();
        
        body.add("inputFile", fileResource);
        body.add("outputFile", outputFileName);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                containerUrl + "/api/convert",
                requestEntity,
                String.class
        );
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new FileConversionException(
                    "Container conversion failed with status: " + response.getStatusCode());
        }
        
        log.info("Conversion completed successfully in Podman container");
    }
    
    /**
     * Cleanup container and remove it
     */
    private void cleanupContainer(String containerId) {
        try {
            // Stop container
            ProcessBuilder stopPb = new ProcessBuilder("podman", "stop", containerId);
            Process stopProcess = stopPb.start();
            stopProcess.waitFor();
            log.debug("Stopped Podman container {}", containerId);
            
            // If cleanup is enabled, the container was created WITH --rm flag and will auto-remove
            // If cleanup is disabled, the container was created WITHOUT --rm flag and won't auto-remove
            // So we don't need to do anything here - either way it's handled correctly
            if (config.isCleanupEnabled()) {
                log.info("Stopped Podman container {} (will auto-remove due to --rm flag)", containerId);
            } else {
                log.info("Stopped Podman container {} (persisted, not removed)", containerId);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup Podman container {}: {}", containerId, e.getMessage());
        }
    }
    
    /**
     * Find an available port for the container
     */
    private int findAvailablePort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return 30000 + (int) (Math.random() * 10000);
        }
    }
}
