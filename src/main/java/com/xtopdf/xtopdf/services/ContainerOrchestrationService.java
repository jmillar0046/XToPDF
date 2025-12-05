package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for orchestrating containers for file conversion jobs.
 * Uses hexagonal architecture with ContainerRuntimePort abstraction,
 * allowing easy switching between Docker, Podman, or other container runtimes.
 * 
 * Each conversion runs in an isolated container that is created on-demand
 * and cleaned up after the job completes.
 */
@Service
@Slf4j
@AllArgsConstructor
public class ContainerOrchestrationService {
    
    private final ContainerRuntimePort containerRuntime;
    
    /**
     * Check if container orchestration is enabled
     */
    public boolean isEnabled() {
        return containerRuntime.isEnabled();
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
        containerRuntime.executeInContainer(inputFile, outputFile, converterLogic);
    }
    
    /**
     * Get container runtime information for diagnostics
     */
    public String getRuntimeInfo() {
        return containerRuntime.getRuntimeInfo();
    }
    
    /**
     * @deprecated Use getRuntimeInfo() instead
     */
    @Deprecated
    public String getDockerInfo() {
        return getRuntimeInfo();
    }
}
