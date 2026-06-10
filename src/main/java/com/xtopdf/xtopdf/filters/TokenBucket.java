package com.xtopdf.xtopdf.filters;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple token bucket rate limiter.
 *
 * <p>The bucket starts full with {@code capacity} tokens. Each call to {@link #tryConsume()}
 * attempts to take one token. Tokens are refilled to capacity after the configured window
 * has elapsed since the last refill.</p>
 *
 * <p>This class is thread-safe.</p>
 */
class TokenBucket {

    private final int capacity;
    private final long windowMillis;
    private final AtomicInteger tokens;
    private final AtomicLong lastRefillTimestamp;

    /**
     * Creates a new token bucket with the given capacity and refill window.
     *
     * @param capacity the maximum number of tokens (requests) allowed per window
     * @param window   the duration of the time window before tokens are refilled
     */
    TokenBucket(int capacity, Duration window) {
        this.capacity = capacity;
        this.windowMillis = window.toMillis();
        this.tokens = new AtomicInteger(capacity);
        this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());
    }

    /**
     * Attempts to consume one token from the bucket.
     *
     * <p>If the time window has elapsed since the last refill, the bucket is refilled
     * to capacity before attempting to consume.</p>
     *
     * @return {@code true} if a token was available and consumed, {@code false} if the
     *         bucket is exhausted (rate limit exceeded)
     */
    boolean tryConsume() {
        synchronized (this) {
            refillIfNeeded();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }
    }

    private void refillIfNeeded() {
        long now = System.currentTimeMillis();
        long lastRefill = lastRefillTimestamp.get();
        if (now - lastRefill >= windowMillis) {
            if (lastRefillTimestamp.compareAndSet(lastRefill, now)) {
                tokens.set(capacity);
            }
        }
    }
}
