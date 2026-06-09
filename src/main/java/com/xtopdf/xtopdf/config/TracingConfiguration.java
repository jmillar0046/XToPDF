package com.xtopdf.xtopdf.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for distributed tracing using Micrometer Observation API
 * with OpenTelemetry bridge.
 * 
 * Enables @Observed annotation support for creating trace spans on methods.
 * Trace propagation is handled automatically by Spring Boot Actuator's
 * auto-configuration with the micrometer-tracing-bridge-otel dependency.
 */
@Configuration
public class TracingConfiguration {

    /**
     * Enables the @Observed annotation AOP aspect, allowing methods annotated
     * with @Observed to automatically create observation spans for tracing.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
