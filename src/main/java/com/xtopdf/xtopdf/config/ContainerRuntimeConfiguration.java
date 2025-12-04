package com.xtopdf.xtopdf.config;

import com.xtopdf.xtopdf.adapters.container.DockerContainerAdapter;
import com.xtopdf.xtopdf.adapters.container.PodmanContainerAdapter;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for container runtime selection.
 * Uses hexagonal architecture to allow easy switching between different
 * container runtimes (Docker, Podman, etc.) without changing business logic.
 * 
 * To switch from Podman to Docker, simply change the configuration:
 * container.orchestration.runtime=docker
 */
@Configuration
@Slf4j
public class ContainerRuntimeConfiguration {
    
    @Bean
    public ContainerRuntimePort containerRuntimePort(ContainerOrchestrationConfig config) {
        ContainerConfig containerConfig = buildContainerConfig(config);
        
        String runtime = config.getRuntime().toLowerCase();
        log.info("Initializing container runtime adapter: {}", runtime);
        
        return switch (runtime) {
            case "podman" -> new PodmanContainerAdapter(containerConfig, config.isEnabled());
            case "docker" -> new DockerContainerAdapter(containerConfig, config.isEnabled());
            default -> {
                log.warn("Unknown runtime '{}', falling back to Podman", runtime);
                yield new PodmanContainerAdapter(containerConfig, config.isEnabled());
            }
        };
    }
    
    /**
     * Build ContainerConfig from ContainerOrchestrationConfig
     */
    private ContainerConfig buildContainerConfig(ContainerOrchestrationConfig config) {
        return ContainerConfig.builder()
                .imageName(config.getImage().getFullName())
                .memoryLimit(config.getMemoryLimit())
                .cpuLimit(config.getCpuLimit())
                .timeoutSeconds(config.getTimeoutSeconds())
                .cleanupEnabled(config.isCleanupEnabled())
                .containerPort(config.getContainerPort())
                .build();
    }
}
