package com.xtopdf.xtopdf.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test verifying that tracing is configured correctly.
 * Validates: Requirements 18.4, 18.5
 */
@SpringBootTest
class TracingConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObservationRegistry observationRegistry;

    @Test
    @DisplayName("Spring context loads with ObservationRegistry bean")
    void contextLoadsWithObservationRegistry() {
        assertThat(observationRegistry).isNotNull();
    }

    @Test
    @DisplayName("ObservedAspect bean is registered for @Observed annotation support")
    void observedAspectBeanIsRegistered() {
        assertThat(applicationContext.getBean(ObservedAspect.class)).isNotNull();
    }

    @Test
    @DisplayName("TracingConfiguration is loaded in the application context")
    void tracingConfigurationIsLoaded() {
        assertThat(applicationContext.getBean(TracingConfiguration.class)).isNotNull();
    }

    @Test
    @DisplayName("ObservationRegistry is not a no-op instance")
    void observationRegistryIsNotNoOp() {
        // The registry should be a functioning instance, not the NOOP singleton
        assertThat(observationRegistry).isNotSameAs(ObservationRegistry.NOOP);
    }
}
