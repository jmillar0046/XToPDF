package com.xtopdf.xtopdf.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that adds Deprecation and Sunset headers to responses for non-versioned API paths.
 * Encourages clients to migrate to the /v1/ prefixed endpoints.
 * 
 * Headers added:
 * - Deprecation: true
 * - Sunset: date when the non-versioned endpoints will be removed
 * - Link: points to the versioned equivalent with rel="successor-version"
 */
@Component
@Slf4j
public class DeprecationHeaderFilter extends OncePerRequestFilter {

    @Value("${xtopdf.deprecation.sunset-date:2026-01-01}")
    private String sunsetDate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var requestUri = request.getRequestURI();

        // Only add deprecation headers to non-versioned API paths
        if (isDeprecatedPath(requestUri)) {
            response.setHeader("Deprecation", "true");
            response.setHeader("Sunset", sunsetDate);

            // Strip CRLF to prevent header injection
            var sanitizedUri = requestUri.replaceAll("[\\r\\n]", "");
            var versionedPath = "/v1" + sanitizedUri;
            response.setHeader("Link", "<" + versionedPath + ">; rel=\"successor-version\"");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path is a deprecated (non-versioned) API path.
     */
    public boolean isDeprecatedPath(String uri) {
        return uri != null
                && uri.startsWith("/api/")
                && !uri.startsWith("/v1/");
    }
}
