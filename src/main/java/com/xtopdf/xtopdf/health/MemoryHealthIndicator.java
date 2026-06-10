package com.xtopdf.xtopdf.health;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator that checks JVM heap memory usage percentage.
 * Reports UP when usage is below 80%, WARNING between 80-90%, and DOWN above 90%.
 * Uses hysteresis to prevent flapping — only reports DOWN after 3 consecutive critical checks.
 */
@Component
@Slf4j
public class MemoryHealthIndicator implements HealthIndicator {

    private static final double WARNING_THRESHOLD = 0.80;
    private static final double CRITICAL_THRESHOLD = 0.90;
    private static final int CONSECUTIVE_CRITICAL_THRESHOLD = 3;

    private final AtomicInteger consecutiveCriticalCount = new AtomicInteger(0);

    @Override
    public Health health() {
        var runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double usagePercentage = (double) usedMemory / maxMemory;

        var builder = buildHealthStatus(usagePercentage);

        builder.withDetail("maxMemoryBytes", maxMemory)
                .withDetail("usedMemoryBytes", usedMemory)
                .withDetail("freeMemoryBytes", maxMemory - usedMemory)
                .withDetail("usagePercentage", String.format("%.1f%%", usagePercentage * 100));

        return builder.build();
    }

    Health.Builder buildHealthStatus(double usagePercentage) {
        if (usagePercentage >= CRITICAL_THRESHOLD) {
            int count = consecutiveCriticalCount.incrementAndGet();
            log.warn("Memory usage critical: {}% (consecutive count: {})",
                    String.format("%.1f", usagePercentage * 100), count);
            if (count >= CONSECUTIVE_CRITICAL_THRESHOLD) {
                return Health.down()
                        .withDetail("reason", String.format("Critical: %.1f%% heap used (%d consecutive checks)",
                                usagePercentage * 100, count));
            }
            // Below threshold for consecutive critical checks — report as WARNING instead of DOWN
            return Health.status("WARNING")
                    .withDetail("reason", String.format("Critical: %.1f%% heap used (count %d/%d before DOWN)",
                            usagePercentage * 100, count, CONSECUTIVE_CRITICAL_THRESHOLD));
        } else if (usagePercentage >= WARNING_THRESHOLD) {
            consecutiveCriticalCount.set(0);
            log.warn("Memory usage elevated: {}%", String.format("%.1f", usagePercentage * 100));
            return Health.status("WARNING")
                    .withDetail("reason", String.format("Warning: %.1f%% heap used", usagePercentage * 100));
        }
        consecutiveCriticalCount.set(0);
        return Health.up();
    }
}
