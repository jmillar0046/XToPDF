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
    void buildHealthStatusReturnsDownWhenAboveCriticalThreshold() {
        var builder = indicator.buildHealthStatus(0.95);
        var health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("reason");
    }

    @Test
    void buildHealthStatusAtExactWarningThresholdReturnsWarning() {
        var builder = indicator.buildHealthStatus(0.80);
        var health = builder.build();

        assertThat(health.getStatus()).isEqualTo(new Status("WARNING"));
    }

    @Test
    void buildHealthStatusAtExactCriticalThresholdReturnsDown() {
        var builder = indicator.buildHealthStatus(0.90);
        var health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
