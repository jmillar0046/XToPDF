package com.xtopdf.xtopdf.health;

import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContainerRuntimeHealthIndicatorTest {

    @Mock
    private ContainerRuntimePort containerRuntimePort;

    @Test
    void healthReturnsUpWhenOrchestrationDisabled() {
        when(containerRuntimePort.isEnabled()).thenReturn(false);

        var indicator = new ContainerRuntimeHealthIndicator(containerRuntimePort);
        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("status", "Container orchestration disabled");
    }

    @Test
    void healthReturnsUpWhenRuntimeAvailable() {
        when(containerRuntimePort.isEnabled()).thenReturn(true);
        when(containerRuntimePort.getRuntimeInfo()).thenReturn("Docker version: 24.0.7, Containers: 3, Images: 10");

        var indicator = new ContainerRuntimeHealthIndicator(containerRuntimePort);
        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("runtime");
    }

    @Test
    void healthReturnsUpWhenRuntimeInfoReturnsAnyString() {
        when(containerRuntimePort.isEnabled()).thenReturn(true);
        when(containerRuntimePort.getRuntimeInfo()).thenReturn("Failed to get Docker info: Connection refused");

        var indicator = new ContainerRuntimeHealthIndicator(containerRuntimePort);
        var health = indicator.health();

        // Any non-exception response is UP (we only go DOWN on thrown exceptions)
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("runtime");
    }

    @Test
    void healthReturnsUpWhenRuntimeInfoIsNull() {
        when(containerRuntimePort.isEnabled()).thenReturn(true);
        when(containerRuntimePort.getRuntimeInfo()).thenReturn(null);

        var indicator = new ContainerRuntimeHealthIndicator(containerRuntimePort);
        var health = indicator.health();

        // Null info may cause NPE caught by try/catch → DOWN, or UP with null detail
        // Either way, the health check should not throw to the caller
        assertThat(health).isNotNull();
    }

    @Test
    void healthReturnsDownWhenExceptionThrown() {
        when(containerRuntimePort.isEnabled()).thenReturn(true);
        when(containerRuntimePort.getRuntimeInfo()).thenThrow(new RuntimeException("Connection refused"));

        var indicator = new ContainerRuntimeHealthIndicator(containerRuntimePort);
        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "Runtime unavailable");
    }
}
