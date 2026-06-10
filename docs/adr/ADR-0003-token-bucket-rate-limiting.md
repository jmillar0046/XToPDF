# ADR-0003: Use Token Bucket Algorithm for Rate Limiting

## Status

Accepted

## Date

2025-01-15

## Context

XToPDF exposes REST endpoints for file conversion — an inherently resource-intensive operation. Without rate limiting:

- A single client could monopolize server resources with rapid-fire requests
- Memory and CPU spikes from concurrent conversions could cause OOM or degraded service
- No protection against accidental or intentional denial-of-service

We need a rate limiting strategy that:
- Is fair to legitimate users while blocking abusive patterns
- Allows short bursts of traffic (batch uploads) without penalizing normal usage
- Is simple to implement, test, and configure
- Works per-client (IP-based) without external dependencies

## Decision

We implement the **token bucket algorithm** for rate limiting:

- Each client IP gets a bucket with a configurable capacity (default: 100 tokens)
- Tokens refill at a fixed rate (default: 100 tokens per 60 seconds)
- Each request consumes one token
- When the bucket is empty, requests are rejected with HTTP 429 and a `Retry-After` header
- Buckets are stored in a `ConcurrentHashMap` keyed by IP address
- Inactive buckets are cleaned up periodically to prevent memory leaks

Implementation:
- `TokenBucket` — thread-safe bucket with atomic operations
- `RateLimiterService` — manages buckets per IP, handles cleanup
- `RateLimitingFilter` — servlet filter that intercepts requests, extracts IP, checks rate limit

Configuration via `application.properties`:
```properties
xtopdf.rate-limit.enabled=true
xtopdf.rate-limit.requests-per-minute=100
xtopdf.rate-limit.window-seconds=60
```

## Consequences

### Positive

- Burst-friendly: clients can send a batch of requests up to bucket capacity without throttling
- Smooth rate enforcement: refill is gradual, not reset-based
- Thread-safe: uses atomic operations in `TokenBucket`
- No external dependencies: pure in-memory implementation
- Configurable: rates adjustable per deployment without code changes
- Testable: property-based tests verify token accounting correctness

### Negative

- In-memory only: rate limits not shared across multiple instances (single-node)
- IP-based: doesn't handle shared IPs (NAT, proxies) well without X-Forwarded-For parsing
- No per-endpoint differentiation in current implementation (flat rate across all endpoints)

### Neutral

- Cleanup of stale buckets runs on a scheduled interval
- Rate limit headers (`X-RateLimit-Remaining`, `Retry-After`) included in responses

## Alternatives Considered

| Alternative | Pros | Cons | Reason for Rejection |
|-------------|------|------|------|
| Fixed window counter | Simplest implementation | Burst problem at window boundaries, unfair | Allows 2x burst at window edges |
| Sliding window log | Precise, no boundary issues | High memory per client (stores timestamps) | Memory overhead unacceptable at scale |
| Sliding window counter | Good balance | More complex than token bucket | Token bucket achieves same goal more intuitively |
| Redis-based (distributed) | Works across instances | External dependency, added latency | Over-engineered for single-instance MVP |
| Spring Cloud Gateway rate limiter | Full-featured | Requires gateway infrastructure | Adds deployment complexity |

## References

- `src/main/java/com/xtopdf/xtopdf/filters/TokenBucket.java`
- `src/main/java/com/xtopdf/xtopdf/filters/RateLimitFilter.java`
- Token Bucket Algorithm — Wikipedia
