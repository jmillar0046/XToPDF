package com.xtopdf.xtopdf.ports;

import lombok.Builder;

/**
 * Immutable value object for container runtime configuration.
 * This abstraction works with any container runtime (Docker, Podman, etc.)
 * and hides runtime-specific implementation details.
 *
 * @param imageName       The container image name with tag (e.g., "xtopdf-converter:latest")
 * @param memoryLimit     Memory limit for the container (e.g., "512m", "1g")
 * @param cpuLimit        CPU limit for the container (number of CPUs)
 * @param timeoutSeconds  Timeout in seconds for container execution
 * @param cleanupEnabled  Whether to automatically cleanup containers after execution
 * @param containerPort   Port to expose from the container (typically 8080 for the conversion service)
 */
@Builder
public record ContainerConfig(
    String imageName,
    String memoryLimit,
    int cpuLimit,
    int timeoutSeconds,
    boolean cleanupEnabled,
    int containerPort
) {}
