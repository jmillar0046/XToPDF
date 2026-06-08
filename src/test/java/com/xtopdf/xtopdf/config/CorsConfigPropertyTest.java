package com.xtopdf.xtopdf.config;

import net.jqwik.api.*;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for CORS denial when unconfigured.
 *
 * Feature: codebase-hardening, Property 10: CORS denial when unconfigured
 *
 * For any HTTP request with an Origin header, when the xtopdf.cors.allowed-origins
 * property is empty, the response SHALL NOT include Access-Control-Allow-Origin headers
 * (effectively denying cross-origin access).
 *
 * Validates: Requirements 24.3
 */
@Tag("Feature: codebase-hardening, Property 10: CORS denial when unconfigured")
class CorsConfigPropertyTest {

    /**
     * Property 10: When allowed-origins is empty, no CORS mappings are registered.
     *
     * For any Origin header value, when CorsConfig has empty allowedOrigins,
     * addCorsMappings SHALL NOT register any mappings (effectively denying all CORS).
     *
     * Validates: Requirements 24.3
     */
    @Property(tries = 25)
    @Label("CORS denies all origins when allowed-origins is empty")
    void corsDeniesAllOriginsWhenUnconfigured(
            @ForAll("randomOrigins") String origin) {

        // Create CorsConfig with empty allowed origins (unconfigured)
        CorsConfig corsConfig = new CorsConfig(
                List.of(),
                List.of("GET", "POST"),
                3600L
        );

        // Use a test CorsRegistry to capture registrations
        TestCorsRegistry registry = new TestCorsRegistry();
        corsConfig.addCorsMappings(registry);

        // When origins is empty, no mappings should be registered
        assertThat(registry.getRegistrationCount())
                .as("No CORS mappings should be registered when allowed-origins is empty (origin: %s)", origin)
                .isEqualTo(0);
    }

    /**
     * Property 10b: When allowed-origins is configured, CORS mappings ARE registered.
     *
     * Validates: Requirements 24.1, 24.2
     */
    @Property(tries = 25)
    @Label("CORS registers mappings when allowed-origins is configured")
    void corsRegistersMappingsWhenConfigured(
            @ForAll("randomOrigins") String origin) {

        // Create CorsConfig with a configured origin
        CorsConfig corsConfig = new CorsConfig(
                List.of(origin),
                List.of("GET", "POST"),
                3600L
        );

        TestCorsRegistry registry = new TestCorsRegistry();
        corsConfig.addCorsMappings(registry);

        // When origins are configured, mappings should be registered
        assertThat(registry.getRegistrationCount())
                .as("CORS mappings should be registered when allowed-origins contains: %s", origin)
                .isGreaterThan(0);
    }

    // --- Arbitraries ---

    @Provide
    Arbitrary<String> randomOrigins() {
        return Arbitraries.of(
                "http://localhost:3000",
                "https://example.com",
                "https://app.xtopdf.com",
                "http://192.168.1.100:8080",
                "https://evil.attacker.com",
                "http://subdomain.trusted.org",
                "https://www.company.io"
        );
    }

    /**
     * Test helper that counts how many CORS registrations are added.
     */
    private static class TestCorsRegistry extends CorsRegistry {
        private int registrationCount = 0;

        @Override
        public CorsRegistration addMapping(String pathPattern) {
            registrationCount++;
            return super.addMapping(pathPattern);
        }

        int getRegistrationCount() {
            return registrationCount;
        }
    }
}
