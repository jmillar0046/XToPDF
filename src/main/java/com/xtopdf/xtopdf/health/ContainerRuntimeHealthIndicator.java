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
            var info = containerRuntimePort.getRuntimeInfo();
            return Health.up().withDetail("runtime", info).build();
        } catch (Exception e) {
            return Health.down().withDetail("reason", "Runtime unavailable").build();
        }
    }
}
