package com.xtopdf.xtopdf.ports;

import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for container runtime configuration.
 * This abstraction works with any container runtime (Docker, Podman, etc.)
 * and hides runtime-specific implementation details.
 */
@Data
@Builder
public class ContainerConfig {
    
    /**
     * The container image name with tag (e.g., "xtopdf-converter:latest")
     */
    private String imageName;
    
    /**
     * Memory limit for the container (e.g., "512m", "1g")
     */
    private String memoryLimit;
    
    /**
     * CPU limit for the container (number of CPUs)
     */
    private int cpuLimit;
    
    /**
     * Timeout in seconds for container execution
     */
    private int timeoutSeconds;
    
    /**
     * Whether to automatically cleanup containers after execution
     */
    private boolean cleanupEnabled;
    
    /**
     * Port to expose from the container (typically 8080 for the conversion service)
     */
    private int containerPort;
}
