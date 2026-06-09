package com.xtopdf.xtopdf.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter that restricts requests per client IP using a token bucket algorithm.
 *
 * <p>Each client IP gets its own token bucket with a configurable capacity and refill window.
 * When the bucket is exhausted, subsequent requests receive HTTP 429 (Too Many Requests)
 * with a Retry-After header.</p>
 *
 * <p>The filter can be disabled entirely via configuration.</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Value("${xtopdf.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${xtopdf.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Value("${xtopdf.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        TokenBucket bucket = buckets.computeIfAbsent(clientIp,
                k -> new TokenBucket(requestsPerMinute, Duration.ofSeconds(windowSeconds)));

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.setContentType("application/json");
            response.getWriter().write("{\"errorCode\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Rate limit exceeded\"}");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
