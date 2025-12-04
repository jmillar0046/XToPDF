package com.xtopdf.xtopdf.ports;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Port interface for container runtime operations.
 * This abstraction allows switching between different container runtimes
 * (Docker, Podman, containerd, etc.) without changing the core business logic.
 * 
 * Follows hexagonal architecture pattern where this interface is a "port"
 * and specific implementations (DockerAdapter, PodmanAdapter) are "adapters".
 */
public interface ContainerRuntimePort {
    
    /**
     * Execute a conversion job in an isolated container.
     * 
     * @param inputFile The input file to convert
     * @param outputFile The desired output file path
     * @param converterLogic The conversion logic to execute when container orchestration is disabled
     * @throws FileConversionException if the conversion fails
     */
    void executeInContainer(MultipartFile inputFile, String outputFile, 
                           Runnable converterLogic) throws FileConversionException;
    
    /**
     * Check if container orchestration is enabled.
     * 
     * @return true if container orchestration is enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Get diagnostic information about the container runtime.
     * 
     * @return String containing runtime information (version, status, etc.)
     */
    String getRuntimeInfo();
}
