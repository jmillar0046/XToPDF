# ADR-0004: Use Caffeine for Conversion Result Caching

## Status

Accepted

## Date

2025-01-15

## Context

File conversion is CPU and I/O intensive. Repeated conversions of the same file (same content, same parameters) waste resources. We need a caching layer that:

- Avoids redundant conversions for identical inputs
- Respects memory constraints (bounded cache size)
- Evicts stale entries automatically (TTL-based)
- Works without external infrastructure for local development
- Can be extended to distributed caching (Redis) for multi-instance deployments

## Decision

We use **Caffeine** as the primary local caching library, integrated via Spring Boot's caching abstraction:

- **Cache key:** SHA-256 hash of file content + conversion parameters
- **Cache value:** path to the converted PDF file
- **Eviction:** LRU (least recently used) when max size is reached
- **Expiration:** TTL-based (configurable, default: 1 hour)
- **Backend:** Caffeine for local cache, with optional Redis for distributed deployments

Integration:
- `CacheConfig` configures Caffeine via Spring's `@EnableCaching`
- `ConversionCacheService` provides get/put operations with hash calculation
- `FileConversionService` checks cache before conversion, stores result after

Configuration:
```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=3600s
xtopdf.cache.enabled=true
```

## Consequences

### Positive

- Near-zero latency for cache hits (in-memory, no network round-trip)
- Automatic eviction prevents unbounded memory growth
- SHA-256 content hashing ensures correct cache hits regardless of filename
- Spring Cache abstraction allows transparent backend swap (Caffeine → Redis)
- High concurrency performance: Caffeine uses striped locks and write buffers
- No external dependency for single-instance deployments

### Negative

- Local cache not shared across instances (cache misses after scaling)
- File-based cache values require cleanup coordination with temp file scheduler
- Memory consumed by cache reduces memory available for conversions
- Cache invalidation limited to TTL — no explicit invalidation on source file changes

### Neutral

- Cache metrics exposed via Micrometer (hit rate, miss rate, eviction count)
- Cache is entirely optional — disabling it doesn't affect correctness

## Alternatives Considered

| Alternative | Pros | Cons | Reason for Rejection |
|-------------|------|------|------|
| Guava Cache | Familiar API, widely used | Deprecated in favor of Caffeine, lower performance | Caffeine is the successor with better throughput |
| EHCache | Disk overflow, clustering | Complex configuration, heavyweight | Over-featured for our use case |
| Redis only | Distributed, persistent | External dependency, network latency, ops overhead | Too heavy for local/dev; used as optional tier |
| No caching | Simplest, no complexity | Repeated conversions waste CPU/IO | Unacceptable for production workloads |
| Spring @Cacheable only | Zero code, annotation-driven | Less control over key generation, harder to test | Need explicit control over content-based keys |

## References

- Caffeine GitHub: https://github.com/ben-manes/caffeine
- `src/main/java/com/xtopdf/xtopdf/config/CacheConfig.java`
- Spring Boot Cache documentation
