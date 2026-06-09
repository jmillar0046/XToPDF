package com.xtopdf.xtopdf.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    private static final String SUNSET_DATE = LocalDate.now().plusMonths(6)
            .format(DateTimeFormatter.ISO_DATE);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var requestUri = request.getRequestURI();

        // Only add deprecation headers to non-versioned API paths
        if (isDeprecatedPath(requestUri)) {
            response.setHeader("Deprecation", "true");
            response.setHeader("Sunset", SUNSET_DATE);

            // Add Link header pointing to the versioned equivalent
            var versionedPath = "/v1" + requestUri;
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
