package com.xtopdf.xtopdf.adapters.container;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.nio.file.Files;
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
    
    // Port range configuration for fallback when dynamic port allocation fails
    private static final int PORT_RANGE_START = 30000;
    private static final int PORT_RANGE_SIZE = 10000;
    private static final int MAX_PORT_RETRIES = 3;
    
    private final ContainerConfig config;
    private final RestTemplate restTemplate;
    private final boolean enabled;
    
    public PodmanContainerAdapter(ContainerConfig config, boolean enabled) {
        this.config = config;
        this.enabled = enabled;
        
        // Configure RestTemplate with timeouts from config
        var requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMs = config.timeoutSeconds() * 1000;
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(requestFactory);
        
        if (enabled) {
            // Verify Podman is installed using ProcessBuilder (array-based arguments)
            try {
                ProcessBuilder pb = new ProcessBuilder("podman", "--version");
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("Podman container adapter initialized with image: {}", config.imageName());
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
            if (containerId != null && config.cleanupEnabled()) {
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
            ProcessBuilder pb = new ProcessBuilder("podman", "version");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder info = new StringBuilder("Podman: ");
            String line = reader.readLine();
            if (line != null) {
                info.append(line);
            }
            return info.toString();
        } catch (Exception e) {
            return "Failed to get Podman info: " + e.getMessage();
        }
    }
    
    /**
     * Create and start a Podman container for conversion, with port retry logic.
     * Retries up to MAX_PORT_RETRIES times on port binding failures.
     */
    private String createAndStartContainer(int hostPort) throws IOException, InterruptedException, FileConversionException {
        IOException lastException = null;
        
        for (int attempt = 0; attempt < MAX_PORT_RETRIES; attempt++) {
            int portToUse = (attempt == 0) ? hostPort : findAvailablePort();
            try {
                return doCreateAndStartContainer(portToUse);
            } catch (IOException e) {
                if (isPortConflict(e)) {
                    lastException = e;
                    log.warn("Port {} conflict on attempt {}/{}, retrying with new port", 
                            portToUse, attempt + 1, MAX_PORT_RETRIES);
                } else {
                    throw e;
                }
            }
        }
        throw new FileConversionException("Failed to allocate port after " + MAX_PORT_RETRIES + " attempts", lastException);
    }
    
    private boolean isPortConflict(IOException e) {
        if (e instanceof BindException) return true;
        String message = e.getMessage();
        return message != null && (message.contains("port") || message.contains("address already in use") 
                || message.contains("bind"));
    }
    
    private String doCreateAndStartContainer(int hostPort) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("podman");
        command.add("run");
        command.add("-d"); // Detached mode
        // Only auto-remove if cleanup is enabled
        if (config.cleanupEnabled()) {
            command.add("--rm");
        }
        command.add("-p");
        command.add(hostPort + ":" + config.containerPort());
        command.add("--memory=" + config.memoryLimit());
        command.add("--cpus=" + config.cpuLimit());
        command.add(config.imageName());
        
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
        
        log.info("Created Podman container {} for conversion job on port {}", containerId, hostPort);
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
     * Execute the conversion via container's REST API.
     * Retrieves the converted PDF as bytes, validates magic header, and writes to output file.
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
        
        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    containerUrl + "/api/convert",
                    requestEntity,
                    byte[].class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new FileConversionException(
                        "Container conversion failed with status: " + response.getStatusCode());
            }
            
            byte[] pdfBytes = response.getBody();
            
            // Validate response contains valid PDF content
            ContainerPdfValidator.validatePdfResponse(pdfBytes);
            
            // Write PDF bytes to output file
            Files.write(outputFileObj.toPath(), pdfBytes);
            
            log.info("Conversion completed successfully in Podman container");
        } catch (ResourceAccessException e) {
            throw new FileConversionException("Container conversion timed out: " + e.getMessage(), e);
        }
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
            if (config.cleanupEnabled()) {
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
            return PORT_RANGE_START + (int) (Math.random() * PORT_RANGE_SIZE);
        }
    }
}
