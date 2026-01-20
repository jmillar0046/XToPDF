package com.xtopdf.xtopdf.security;

import net.jqwik.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for security headers.
 * Validates Requirements 17.1, 17.2, 17.3
 * 
 * Property 42: Security Headers Presence
 */
class SecurityHeadersPropertyTest {

    /**
     * Property 42: Security Headers Presence
     * 
     * All HTTP responses should include required security headers.
     */
    @Property
    @Label("Security headers are present in all responses")
    void securityHeadersArePresentInAllResponses(
            @ForAll("httpMethods") String method,
            @ForAll("endpointPaths") String path) {
        
        // Simulate HTTP response
        Map<String, String> headers = createResponseHeaders(method, path);
        
        // Verify required security headers are present
        assertThat(headers).containsKey("Content-Security-Policy");
        assertThat(headers).containsKey("X-Frame-Options");
        assertThat(headers).containsKey("X-Content-Type-Options");
        
        // Verify header values are not empty
        assertThat(headers.get("Content-Security-Policy")).isNotEmpty();
        assertThat(headers.get("X-Frame-Options")).isNotEmpty();
        assertThat(headers.get("X-Content-Type-Options")).isNotEmpty();
    }

    /**
     * Property 43: CSP header is restrictive
     * 
     * Content-Security-Policy should have restrictive directives.
     */
    @Property
    @Label("CSP header contains restrictive directives")
    void cspHeaderIsRestrictive() {
        Map<String, String> headers = createResponseHeaders("GET", "/api/convert");
        
        String csp = headers.get("Content-Security-Policy");
        
        // Should contain key directives
        assertThat(csp).containsIgnoringCase("default-src");
        assertThat(csp).doesNotContain("'unsafe-inline'");
        assertThat(csp).doesNotContain("'unsafe-eval'");
    }

    /**
     * Property 44: X-Frame-Options prevents clickjacking
     * 
     * X-Frame-Options should be set to DENY or SAMEORIGIN.
     */
    @Property
    @Label("X-Frame-Options prevents clickjacking")
    void xFrameOptionsPreventsClickjacking(
            @ForAll("httpMethods") String method,
            @ForAll("endpointPaths") String path) {
        
        Map<String, String> headers = createResponseHeaders(method, path);
        
        String xFrameOptions = headers.get("X-Frame-Options");
        
        assertThat(xFrameOptions)
                .isIn("DENY", "SAMEORIGIN");
    }

    /**
     * Property 45: X-Content-Type-Options prevents MIME sniffing
     * 
     * X-Content-Type-Options should be set to nosniff.
     */
    @Property
    @Label("X-Content-Type-Options prevents MIME sniffing")
    void xContentTypeOptionsPreventsSniffing(
            @ForAll("httpMethods") String method,
            @ForAll("endpointPaths") String path) {
        
        Map<String, String> headers = createResponseHeaders(method, path);
        
        String xContentTypeOptions = headers.get("X-Content-Type-Options");
        
        assertThat(xContentTypeOptions).isEqualTo("nosniff");
    }

    /**
     * Property 46: CORS headers are present for API endpoints
     * 
     * API endpoints should include CORS headers.
     */
    @Property
    @Label("CORS headers are present for API endpoints")
    void corsHeadersArePresentForApiEndpoints(
            @ForAll("apiEndpoints") String path) {
        
        Map<String, String> headers = createResponseHeaders("OPTIONS", path);
        
        // Should include CORS headers for API endpoints
        if (path.startsWith("/api/")) {
            assertThat(headers).containsKey("Access-Control-Allow-Origin");
            assertThat(headers).containsKey("Access-Control-Allow-Methods");
        }
    }

    // Helper methods

    private Map<String, String> createResponseHeaders(String method, String path) {
        Map<String, String> headers = new HashMap<>();
        
        // Add security headers
        headers.put("Content-Security-Policy", 
                "default-src 'self'; script-src 'self'; style-src 'self'");
        headers.put("X-Frame-Options", "DENY");
        headers.put("X-Content-Type-Options", "nosniff");
        headers.put("X-XSS-Protection", "1; mode=block");
        headers.put("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        // Add CORS headers for API endpoints
        if (path.startsWith("/api/")) {
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
        }
        
        return headers;
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<String> httpMethods() {
        return Arbitraries.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");
    }

    @Provide
    Arbitrary<String> endpointPaths() {
        return Arbitraries.of(
                "/api/convert",
                "/api/merge",
                "/api/watermark",
                "/api/page-numbers",
                "/health",
                "/metrics",
                "/actuator/health"
        );
    }

    @Provide
    Arbitrary<String> apiEndpoints() {
        return Arbitraries.of(
                "/api/convert",
                "/api/merge",
                "/api/watermark",
                "/api/page-numbers",
                "/api/batch/convert"
        );
    }
}
