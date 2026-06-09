package com.xtopdf.xtopdf.versioning;

import com.xtopdf.xtopdf.filters.DeprecationHeaderFilter;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for API versioning.
 * 
 * Property 52: API Version Deprecation Header
 * Validates: Requirements 24.3
 */
@Tag("Feature: api-versioning, Property 52: API Version Deprecation Header")
class ApiVersioningPropertyTest {

    private final DeprecationHeaderFilter filter = new DeprecationHeaderFilter();

    /**
     * Property 52: API Version Deprecation Header
     * 
     * For any request to a deprecated (non-versioned) API path, the system should
     * identify it as deprecated.
     * 
     * **Validates: Requirements 24.3**
     */
    @Property(tries = 25)
    @Label("Non-versioned API paths are identified as deprecated")
    void nonVersionedApiPathsAreDeprecated(
            @ForAll("apiEndpoints") String endpoint) {
        var deprecatedPath = "/api/" + endpoint;

        assertThat(filter.isDeprecatedPath(deprecatedPath))
                .as("Path %s should be deprecated", deprecatedPath)
                .isTrue();
    }

    /**
     * Versioned paths (/v1/api/...) should NOT be identified as deprecated.
     */
    @Property(tries = 25)
    @Label("Versioned API paths are not deprecated")
    void versionedApiPathsAreNotDeprecated(
            @ForAll("apiEndpoints") String endpoint) {
        var versionedPath = "/v1/api/" + endpoint;

        assertThat(filter.isDeprecatedPath(versionedPath))
                .as("Path %s should not be deprecated", versionedPath)
                .isFalse();
    }

    /**
     * Non-API paths should not be marked as deprecated.
     */
    @Property(tries = 25)
    @Label("Non-API paths are not deprecated")
    void nonApiPathsAreNotDeprecated(
            @ForAll("nonApiPaths") String path) {

        assertThat(filter.isDeprecatedPath(path))
                .as("Path %s should not be deprecated", path)
                .isFalse();
    }

    /**
     * Null path should not be deprecated.
     */
    @Property(tries = 25)
    @Label("Null path is not deprecated")
    void nullPathIsNotDeprecated() {
        assertThat(filter.isDeprecatedPath(null)).isFalse();
    }

    @Provide
    Arbitrary<String> apiEndpoints() {
        return Arbitraries.of(
                "convert",
                "convert/batch",
                "convert/async",
                "convert/async/123",
                "convert/json",
                "pdf/merge",
                "pdf/add-page-numbers",
                "pdf/add-watermark"
        );
    }

    @Provide
    Arbitrary<String> nonApiPaths() {
        return Arbitraries.of(
                "/actuator/health",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v1/api/convert",
                "/static/index.html",
                "/"
        );
    }
}
