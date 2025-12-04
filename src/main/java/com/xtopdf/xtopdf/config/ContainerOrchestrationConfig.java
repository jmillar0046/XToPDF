package com.xtopdf.xtopdf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for container orchestration settings.
 * Controls how conversion jobs are executed in isolated Docker containers.
 */
@Configuration
@ConfigurationProperties(prefix = "container.orchestration")
@Data
public class ContainerOrchestrationConfig {
    
    /**
     * Enable or disable container orchestration.
     * When false, conversions run in the main application process.
     * When true, each conversion runs in an isolated container.
     */
    private boolean enabled = false;
    
    /**
     * Docker image configuration
     */
    private Image image = new Image();
    
    /**
     * Memory limit for containers (e.g., "512m", "1g")
     */
    private String memoryLimit = "512m";
    
    /**
     * CPU limit for containers (number of CPUs)
     */
    private int cpuLimit = 1;
    
    /**
     * Timeout in seconds for container execution
     */
    private int timeoutSeconds = 300;
    
    /**
     * Enable automatic cleanup of containers after execution
     */
    private boolean cleanupEnabled = true;
    
    @Data
    public static class Image {
        /**
         * Docker image name
         */
        private String name = "xtopdf-converter";
        
        /**
         * Docker image tag
         */
        private String tag = "latest";
        
        /**
         * Get the full image name with tag
         */
        public String getFullName() {
            return name + ":" + tag;
        }
    }
}
