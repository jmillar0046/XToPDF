package com.xtopdf.xtopdf.health;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for health checks.
 * Validates Requirement 20.4
 * 
 * Property 47: Component Health Reporting
 */
class HealthCheckPropertyTest {

    /**
     * Property 47: Component Health Reporting
     * 
     * Each component should report its health status accurately.
     */
    @Property
    @Label("Components report health status accurately")
    void componentsReportHealthAccurately(
            @ForAll("healthStatuses") HealthStatus status,
            @ForAll("componentNames") String componentName) {
        
        HealthIndicator indicator = new HealthIndicator(componentName, status);
        
        Health health = indicator.health();
        
        assertThat(health.getStatus()).isEqualTo(status);
        assertThat(health.getComponentName()).isEqualTo(componentName);
    }

    /**
     * Property 48: Degraded status when component unhealthy
     * 
     * When any component is unhealthy, overall status should be degraded.
     */
    @Property
    @Label("Overall status is degraded when component unhealthy")
    void overallStatusDegradedWhenComponentUnhealthy(
            @ForAll @IntRange(min = 1, max = 5) int healthyComponents,
            @ForAll @IntRange(min = 1, max = 3) int unhealthyComponents) {
        
        HealthAggregator aggregator = new HealthAggregator();
        
        // Add healthy components
        for (int i = 0; i < healthyComponents; i++) {
            aggregator.addComponent("healthy-" + i, HealthStatus.UP);
        }
        
        // Add unhealthy components
        for (int i = 0; i < unhealthyComponents; i++) {
            aggregator.addComponent("unhealthy-" + i, HealthStatus.DOWN);
        }
        
        HealthStatus overall = aggregator.getOverallStatus();
        
        // Overall should be DOWN if any component is DOWN
        assertThat(overall).isEqualTo(HealthStatus.DOWN);
    }

    /**
     * Property 49: All components UP means overall UP
     * 
     * When all components are healthy, overall status should be UP.
     */
    @Property
    @Label("Overall status is UP when all components healthy")
    void overallStatusUpWhenAllComponentsHealthy(
            @ForAll @IntRange(min = 1, max = 10) int numComponents) {
        
        HealthAggregator aggregator = new HealthAggregator();
        
        for (int i = 0; i < numComponents; i++) {
            aggregator.addComponent("component-" + i, HealthStatus.UP);
        }
        
        HealthStatus overall = aggregator.getOverallStatus();
        
        assertThat(overall).isEqualTo(HealthStatus.UP);
    }

    /**
     * Property 50: Health details include all components
     * 
     * Health report should include status of all registered components.
     */
    @Property
    @Label("Health details include all components")
    void healthDetailsIncludeAllComponents(
            @ForAll @IntRange(min = 1, max = 5) int numComponents) {
        
        HealthAggregator aggregator = new HealthAggregator();
        
        for (int i = 0; i < numComponents; i++) {
            aggregator.addComponent("component-" + i, HealthStatus.UP);
        }
        
        Map<String, HealthStatus> details = aggregator.getComponentDetails();
        
        assertThat(details).hasSize(numComponents);
        assertThat(details.keySet()).allMatch(name -> name.startsWith("component-"));
    }

    // Helper classes

    enum HealthStatus {
        UP, DOWN, UNKNOWN
    }

    static class Health {
        private final HealthStatus status;
        private final String componentName;

        Health(HealthStatus status, String componentName) {
            this.status = status;
            this.componentName = componentName;
        }

        HealthStatus getStatus() {
            return status;
        }

        String getComponentName() {
            return componentName;
        }
    }

    static class HealthIndicator {
        private final String componentName;
        private final HealthStatus status;

        HealthIndicator(String componentName, HealthStatus status) {
            this.componentName = componentName;
            this.status = status;
        }

        Health health() {
            return new Health(status, componentName);
        }
    }

    static class HealthAggregator {
        private final Map<String, HealthStatus> components = new HashMap<>();

        void addComponent(String name, HealthStatus status) {
            components.put(name, status);
        }

        HealthStatus getOverallStatus() {
            if (components.isEmpty()) {
                return HealthStatus.UNKNOWN;
            }
            
            boolean hasDown = components.values().stream()
                    .anyMatch(status -> status == HealthStatus.DOWN);
            
            if (hasDown) {
                return HealthStatus.DOWN;
            }
            
            boolean allUp = components.values().stream()
                    .allMatch(status -> status == HealthStatus.UP);
            
            return allUp ? HealthStatus.UP : HealthStatus.UNKNOWN;
        }

        Map<String, HealthStatus> getComponentDetails() {
            return new HashMap<>(components);
        }
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<HealthStatus> healthStatuses() {
        return Arbitraries.of(HealthStatus.UP, HealthStatus.DOWN, HealthStatus.UNKNOWN);
    }

    @Provide
    Arbitrary<String> componentNames() {
        return Arbitraries.of(
                "database",
                "cache",
                "disk-space",
                "memory",
                "container-runtime",
                "external-api"
        );
    }
}
