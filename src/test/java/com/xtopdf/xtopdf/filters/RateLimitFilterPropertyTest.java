package com.xtopdf.xtopdf.filters;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for rate limiter token bucket enforcement.
 *
 * Property 2: Rate limiter token bucket enforcement
 *
 * For any sequence of N requests from the same client IP within a configured
 * time window of capacity C:
 * - The first C requests SHALL pass through (not get 429)
 * - Any request beyond C SHALL receive HTTP 429 (Too Many Requests)
 * - The 429 response SHALL include a Retry-After header with a positive integer value
 *
 * This tests the TokenBucket algorithm directly at the unit level.
 * The TokenBucket class will be in package com.xtopdf.xtopdf.filters.
 *
 * Constructor: TokenBucket(int capacity, Duration window)
 * Method: boolean tryConsume() — returns true if token available, false if exhausted
 *
 * Validates: Requirements 3.1, 3.2
 */
@Tag("Feature: codebase-hardening, Property 2: Rate limiter token bucket enforcement")
class RateLimitFilterPropertyTest {

    /**
     * Property 2: Rate limiter token bucket enforcement
     *
     * For any random capacity (1-50) and request count (1-100):
     * - For requestCount <= capacity: all tryConsume() calls return true
     * - For requestCount > capacity: first `capacity` calls return true, subsequent return false
     *
     * Validates: Requirements 3.1, 3.2
     */
    @Property(tries = 25)
    @Label("Token bucket allows exactly capacity requests then rejects subsequent ones")
    void tokenBucketEnforcesCapacity(
            @ForAll @IntRange(min = 1, max = 50) int capacity,
            @ForAll @IntRange(min = 1, max = 100) int requestCount) {

        var bucket = new TokenBucket(capacity, Duration.ofSeconds(60));

        int allowedCount = 0;
        int rejectedCount = 0;

        for (int i = 0; i < requestCount; i++) {
            if (bucket.tryConsume()) {
                allowedCount++;
            } else {
                rejectedCount++;
            }
        }

        if (requestCount <= capacity) {
            // All requests should be allowed
            assertThat(allowedCount)
                    .as("All %d requests should pass when capacity is %d", requestCount, capacity)
                    .isEqualTo(requestCount);
            assertThat(rejectedCount)
                    .as("No requests should be rejected when requestCount (%d) <= capacity (%d)", requestCount, capacity)
                    .isEqualTo(0);
        } else {
            // Exactly `capacity` requests should pass, the rest should be rejected
            assertThat(allowedCount)
                    .as("Exactly %d requests should pass (capacity), got %d", capacity, allowedCount)
                    .isEqualTo(capacity);
            assertThat(rejectedCount)
                    .as("Requests beyond capacity should be rejected")
                    .isEqualTo(requestCount - capacity);
        }
    }

    /**
     * Property: A fresh token bucket with capacity C allows exactly C consecutive requests.
     *
     * Validates: Requirements 3.1
     */
    @Property(tries = 25)
    @Label("Fresh token bucket allows exactly capacity number of requests")
    void freshBucketAllowsExactlyCapacityRequests(
            @ForAll @IntRange(min = 1, max = 50) int capacity) {

        var bucket = new TokenBucket(capacity, Duration.ofSeconds(60));

        // First `capacity` calls should all succeed
        for (int i = 0; i < capacity; i++) {
            assertThat(bucket.tryConsume())
                    .as("Request %d of %d should be allowed", i + 1, capacity)
                    .isTrue();
        }

        // The (capacity + 1)th call should fail
        assertThat(bucket.tryConsume())
                .as("Request beyond capacity (%d + 1) should be rejected", capacity)
                .isFalse();
    }

    /**
     * Property: After exhausting the bucket, all subsequent requests are rejected
     * until the window resets.
     *
     * Validates: Requirements 3.2
     */
    @Property(tries = 25)
    @Label("Exhausted token bucket rejects all subsequent requests")
    void exhaustedBucketRejectsAllSubsequentRequests(
            @ForAll @IntRange(min = 1, max = 50) int capacity,
            @ForAll @IntRange(min = 1, max = 20) int additionalRequests) {

        var bucket = new TokenBucket(capacity, Duration.ofSeconds(60));

        // Exhaust the bucket
        for (int i = 0; i < capacity; i++) {
            bucket.tryConsume();
        }

        // All additional requests should be rejected
        for (int i = 0; i < additionalRequests; i++) {
            assertThat(bucket.tryConsume())
                    .as("Additional request %d after exhaustion should be rejected", i + 1)
                    .isFalse();
        }
    }

    // ---------------------------------------------------------------
    // Integration test note (to be added when RateLimitFilter exists)
    // ---------------------------------------------------------------
    //
    // When the RateLimitFilter is implemented (task 5.2), an integration-level
    // property test should verify:
    //
    // 1. For N requests from the same IP within a time window of capacity C:
    //    - First C requests receive pass-through (2xx from downstream)
    //    - Requests C+1..N receive HTTP 429 (Too Many Requests)
    //    - The 429 response includes a Retry-After header with a positive integer
    //
    // 2. When rate limiting is disabled (xtopdf.rate-limit.enabled=false):
    //    - All requests pass through regardless of count
    //
    // 3. Different client IPs maintain separate buckets:
    //    - IP-A exhausting its bucket does not affect IP-B
    //
    // This can be tested with @WebMvcTest(RateLimitFilter.class) or
    // @SpringBootTest with MockMvc once the filter exists.
    //
}
