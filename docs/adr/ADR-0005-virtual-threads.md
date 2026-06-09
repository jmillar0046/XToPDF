# ADR-0005: Use Virtual Threads for Async and Batch Processing

## Status

Accepted

## Date

2025-01-15

## Context

XToPDF supports batch conversion (multiple files in one request) and async conversion (submit job, poll for results). Both require concurrent processing:

- Batch: convert N files in parallel, collect results
- Async: execute conversion in background, return job ID immediately

Traditional platform threads are expensive (~1MB stack each) and limited by OS thread count. For a file conversion service handling dozens of concurrent requests, thread pool sizing becomes a critical tuning parameter — too few threads starves throughput, too many causes memory pressure.

Java 21+ introduced virtual threads (Project Loom) as lightweight threads managed by the JVM runtime, with negligible creation cost and near-unlimited scalability.

## Decision

We use **virtual threads** (Java 21+ / finalized in Java 25) for all async and batch processing:

- **Batch processing:** `Executors.newVirtualThreadPerTaskExecutor()` to run each file conversion concurrently
- **Async API:** Virtual thread per submitted job for background processing
- **Structured concurrency:** `StructuredTaskScope` for batch operations that must complete or fail together
- **Concurrency limits:** Semaphore-based limiting (configurable max concurrent conversions) to prevent resource exhaustion despite lightweight threads

Implementation:
- `BatchConversionService` uses virtual threads with a semaphore to cap concurrent conversions
- `AsyncConversionController` submits jobs to a virtual thread executor
- `JobTrackingService` tracks job lifecycle (PENDING → PROCESSING → COMPLETED/FAILED)

Configuration:
```properties
xtopdf.batch.max-concurrent-conversions=10
xtopdf.batch.timeout-per-file-seconds=120
```

## Consequences

### Positive

- No thread pool sizing headaches: virtual threads scale to thousands without tuning
- Simpler code: no callback/future chaining, just blocking I/O in virtual threads
- Lower memory footprint than platform threads (~few KB vs ~1MB per thread)
- Built-in to Java 25: no external library dependencies (no RxJava, no Reactor)
- Structured concurrency ensures no leaked subtasks in batch operations
- Compatible with existing blocking libraries (PDFBox, POI)

### Negative

- Virtual threads don't improve CPU-bound work (conversion is partly CPU-bound)
- `synchronized` blocks can pin virtual threads to carrier threads (must avoid or use ReentrantLock)
- Debugging virtual threads is less mature than platform threads in some IDEs
- Requires Java 21+ (not an issue for this project on Java 25)

### Neutral

- Semaphore is still needed to limit actual resource usage (CPU, memory, disk I/O)
- Virtual threads are invisible at the OS level (can't use OS-level thread monitoring)
- Thread-local usage replaced with ScopedValue where possible

## Alternatives Considered

| Alternative | Pros | Cons | Reason for Rejection |
|-------------|------|------|------|
| Fixed thread pool (platform threads) | Well-understood, mature tooling | Requires tuning, limited concurrency, high memory | Artificial concurrency ceiling, memory waste |
| Project Reactor (reactive) | Non-blocking, back-pressure | Complex programming model, steep learning curve, incompatible with blocking libs | PDFBox/POI are blocking — reactive offers no benefit |
| CompletableFuture chains | No extra dependencies | Callback hell, complex error handling, no structured cancellation | Virtual threads are simpler and achieve same concurrency |
| Kotlin coroutines | Structured concurrency, lightweight | Adds Kotlin dependency, mixed-language codebase | Unnecessary language dependency for a Java project |

## References

- JEP 444: Virtual Threads (Java 21, finalized)
- JEP 480: Structured Concurrency (Java 25, 5th preview)
- `src/main/java/com/xtopdf/xtopdf/controllers/BatchConversionController.java`
- `src/main/java/com/xtopdf/xtopdf/config/BatchConfig.java`
