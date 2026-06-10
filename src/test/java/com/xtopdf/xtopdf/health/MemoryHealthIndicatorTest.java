package com.xtopdf.xtopdf.health;

import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryHealthIndicatorTest {

    private final MemoryHealthIndicator indicator = new MemoryHealthIndicator();

    @Test
    void healthReturnsStatusWithMemoryDetails() {
        var health = indicator.health();

        assertThat(health.getDetails()).containsKeys(
                "maxMemoryBytes", "usedMemoryBytes", "freeMemoryBytes", "usagePercentage");
    }

    @Test
    void healthReturnsUpForNormalMemoryUsage() {
        var health = indicator.health();

        assertThat(health.getStatus()).isIn(Status.UP, new Status("WARNING"));
    }

    @Test
    void buildHealthStatusReturnsUpWhenBelowWarningThreshold() {
        var builder = indicator.buildHealthStatus(0.50);
        var health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void buildHealthStatusReturnsWarningWhenBetweenThresholds() {
        var builder = indicator.buildHealthStatus(0.85);
        var health = builder.build();

        assertThat(health.getStatus()).isEqualTo(new Status("WARNING"));
        assertThat(health.getDetails()).containsKey("reason");
    }

    @Test
    void buildHealthStatusReturnsDownAfterConsecutiveCriticalChecks() {
        // First critical check returns WARNING (count 1/3)
        var builder1 = indicator.buildHealthStatus(0.95);
        assertThat(builder1.build().getStatus()).isEqualTo(new Status("WARNING"));

        // Second critical check returns WARNING (count 2/3)
        var builder2 = indicator.buildHealthStatus(0.95);
        assertThat(builder2.build().getStatus()).isEqualTo(new Status("WARNING"));

        // Third critical check returns DOWN (count 3/3)
        var builder3 = indicator.buildHealthStatus(0.95);
        assertThat(builder3.build().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void buildHealthStatusAtExactWarningThresholdReturnsWarning() {
        var builder = indicator.buildHealthStatus(0.80);
        var health = builder.build();

        assertThat(health.getStatus()).isEqualTo(new Status("WARNING"));
    }

    @Test
    void buildHealthStatusAtExactCriticalThresholdReturnsWarningOnFirstCheck() {
        var builder = indicator.buildHealthStatus(0.90);
        var health = builder.build();

        // First check at critical: returns WARNING due to hysteresis (needs 3 consecutive)
        assertThat(health.getStatus()).isEqualTo(new Status("WARNING"));
    }
}
