package com.xtopdf.xtopdf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CORS configuration for the XToPDF API.
 *
 * <p>By default, when no origins are configured (empty list), cross-origin requests
 * are denied. This is a secure default — origins must be explicitly allowed.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final List<String> allowedOrigins;
    private final List<String> allowedMethods;
    private final long maxAge;

    public CorsConfig(
            @Value("${xtopdf.cors.allowed-origins:}") List<String> allowedOrigins,
            @Value("${xtopdf.cors.allowed-methods:GET,POST}") List<String> allowedMethods,
            @Value("${xtopdf.cors.max-age-seconds:3600}") long maxAge) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.maxAge = maxAge;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return; // Deny by default — no CORS mappings registered
        }

        // Filter out empty strings that Spring might inject for empty property
        var effectiveOrigins = allowedOrigins.stream()
                .filter(o -> o != null && !o.isBlank())
                .toList();

        if (effectiveOrigins.isEmpty()) {
            return; // Still no real origins configured
        }

        registry.addMapping("/api/**")
                .allowedOrigins(effectiveOrigins.toArray(String[]::new))
                .allowedMethods(allowedMethods.toArray(String[]::new))
                .maxAge(maxAge);
    }
}
