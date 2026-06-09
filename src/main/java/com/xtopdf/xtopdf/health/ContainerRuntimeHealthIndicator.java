package com.xtopdf.xtopdf.health;

import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator that checks if the configured container runtime (Docker/Podman) is available.
 * Reports UP when the runtime is reachable, DOWN otherwise.
 * If container orchestration is disabled, reports UP with a note.
 */
@Component
@Slf4j
public class ContainerRuntimeHealthIndicator implements HealthIndicator {

    private final ContainerRuntimePort containerRuntimePort;

    public ContainerRuntimeHealthIndicator(ContainerRuntimePort containerRuntimePort) {
        this.containerRuntimePort = containerRuntimePort;
    }

    @Override
    public Health health() {
        if (!containerRuntimePort.isEnabled()) {
            return Health.up()
                    .withDetail("status", "Container orchestration disabled")
                    .build();
        }

        try {
            var runtimeInfo = containerRuntimePort.getRuntimeInfo();
            if (runtimeInfo != null && !runtimeInfo.startsWith("Failed")) {
                return Health.up()
                        .withDetail("runtime", runtimeInfo)
                        .build();
            } else {
                return Health.down()
                        .withDetail("reason", runtimeInfo != null ? runtimeInfo : "No runtime info available")
                        .build();
            }
        } catch (Exception e) {
            log.warn("Container runtime health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("reason", "Container runtime unavailable")
                    .withException(e)
                    .build();
        }
    }
}
