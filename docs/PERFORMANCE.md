# Performance Tuning Guide

This guide covers performance characteristics, JVM tuning, capacity planning, and benchmarking for XToPDF deployments.

## Performance Characteristics

### Conversion Times by Format

Typical conversion times on a 4-core machine with 4GB heap (representative, not guaranteed):

| Format | Small (<1MB) | Medium (1-10MB) | Large (10-100MB) |
|--------|-------------|-----------------|-------------------|
| DOCX/ODT | 200-500ms | 500ms-2s | 2-10s |
| XLSX/ODS | 300-800ms | 1-5s | 5-30s |
| CSV/TSV | 100-300ms | 500ms-3s | 3-15s (streaming) |
| HTML | 200-600ms | 500ms-2s | 2-8s |
| Markdown | 50-200ms | 200-800ms | 1-3s |
| PNG/JPEG/TIFF | 100-400ms | 400ms-2s | 2-10s |
| SVG | 200-500ms | 500ms-3s | 3-12s |
| CAD (DXF) | 500ms-2s | 2-8s | 8-30s |

### Streaming Mode

CSV/TSV and XLSX files over **10MB** automatically switch to streaming mode:

- **Threshold:** 10,000,000 bytes (10MB)
- **Chunk size:** 1000 rows per chunk (CSV/TSV), SAX-based streaming (XLSX)
- **Memory benefit:** Streaming mode processes data incrementally, keeping heap usage bounded regardless of file size
- **Trade-off:** Streaming adds ~10-20% conversion time overhead due to chunk coordination

Files below the threshold use in-memory processing for faster throughput.

### Memory Usage Patterns

| Operation | Memory Footprint |
|-----------|-----------------|
| Idle service | ~150-200MB heap |
| Single small file conversion | +50-150MB |
| Single large file (in-memory) | +200-500MB |
| Single large file (streaming) | +50-100MB (bounded) |
| Batch of 10 files (parallel) | +500MB-2GB (depends on file sizes) |
| Caffeine cache (100 entries) | +10-50MB (metadata only, PDFs on disk) |

### Parallel Processing

- **Batch endpoint:** Up to 10 files per request, processed with virtual threads
- **Concurrency limit:** Configurable via `xtopdf.batch.parallel-workers` (default: 4)
- **Semaphore-based throttling:** Prevents resource exhaustion even with lightweight virtual threads
- **Timeout per file:** 300 seconds (configurable)

### Rate Limiting Impact

- **Default:** 100 requests/minute per IP (token bucket algorithm)
- **Burst capacity:** Full bucket allows burst of 100 requests immediately
- **Refill rate:** Continuous (~1.67 tokens/second)
- **Overhead:** Negligible (<1ms per request for bucket check)

## JVM Tuning Recommendations

### Heap Size

```bash
# Development / light workload (1-5 concurrent conversions)
JAVA_OPTS="-Xms512m -Xmx2g"

# Production / moderate workload (5-20 concurrent conversions)
JAVA_OPTS="-Xms2g -Xmx4g"

# High-throughput (20+ concurrent conversions, large files)
JAVA_OPTS="-Xms4g -Xmx8g"
```

**Rule of thumb:** Allow 200MB base + 200MB per expected concurrent conversion for medium files, more for large files processed in-memory.

### Garbage Collection

```bash
# G1GC (recommended for most deployments)
JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m"

# ZGC (recommended for low-latency requirements, Java 25)
JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational"

# Shenandoah (alternative low-pause collector)
JAVA_OPTS="-XX:+UseShenandoahGC"
```

**G1GC** is the default and works well for most workloads. Choose **ZGC** when you need consistent sub-10ms GC pauses (e.g., serving real-time API responses alongside conversions). ZGC's generational mode in Java 25 handles the allocation-heavy conversion workload efficiently.

### Virtual Threads

Virtual threads are enabled by default on Java 25. No special JVM flags are needed. Key considerations:

- Virtual threads use carrier threads from the ForkJoinPool (defaults to `availableProcessors()`)
- For CPU-bound conversion work, the carrier pool size is usually optimal at default
- If pinning is observed (e.g., from `synchronized` in third-party libraries), consider:

```bash
# Increase carrier threads if pinning is frequent
JAVA_OPTS="-Djdk.virtualThreadScheduler.parallelism=16"

# Monitor pinning events
JAVA_OPTS="-Djdk.tracePinnedThreads=short"
```

### Recommended Production JVM Configuration

```bash
JAVA_OPTS="\
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/xtopdf/heapdump.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -Djdk.tracePinnedThreads=short \
  --enable-preview"
```

### Container-Specific Settings

When running in Docker/Kubernetes:

```bash
# Let JVM detect container memory limits
JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

This ensures the JVM respects cgroup memory limits and uses 75% of the container's memory allocation for the heap, leaving 25% for native memory, stack space, and the OS.

## Capacity Planning

### Concurrent Users

| Deployment Size | vCPUs | RAM | Concurrent Users | Throughput (files/min) |
|----------------|-------|-----|-----------------|----------------------|
| Small | 2 | 4GB | 5-10 | 20-40 |
| Medium | 4 | 8GB | 20-50 | 60-120 |
| Large | 8 | 16GB | 50-100 | 150-300 |
| XL (multi-node) | 16+ | 32GB+ | 200+ | 500+ |

**Assumptions:** Average file size 2-5MB, mixed format types, G1GC, default configuration.

### File Size Guidelines

| File Size | Recommended Config | Notes |
|-----------|-------------------|-------|
| < 1MB | Default settings | Most formats convert in <500ms |
| 1-10MB | Default settings | May benefit from increased heap |
| 10-50MB | 4GB+ heap, streaming auto-activates for CSV/XLSX | Monitor GC pressure |
| 50-100MB | 8GB+ heap, consider container isolation | Use async endpoint |
| > 100MB | Rejected (configurable limit) | Increase `spring.servlet.multipart.max-file-size` if needed |

### Batch Size Impact

The batch endpoint accepts up to 10 files (configurable via `xtopdf.batch.max-size`):

| Batch Size | Parallel Workers | Expected Duration | Memory Impact |
|-----------|-----------------|-------------------|---------------|
| 1-3 files | 4 | Similar to sequential | Low |
| 4-6 files | 4 | ~2x single file time | Moderate |
| 7-10 files | 4 | ~3x single file time | High |

Increasing `xtopdf.batch.parallel-workers` beyond CPU core count provides diminishing returns for CPU-bound conversions.

### Scaling Strategies

**Vertical Scaling:**
- Increase heap and CPU cores for more concurrent conversions
- Best for: simple deployments, low operational overhead

**Horizontal Scaling:**
- Run multiple XToPDF instances behind a load balancer
- Each instance maintains its own Caffeine cache (no sharing)
- Rate limiting is per-instance (use Redis-based rate limiting for shared limits)
- Best for: high availability, rolling deployments, large workloads

**Container Orchestration Isolation:**
- Enable `container.orchestration.enabled=true` for untrusted files
- Each conversion runs in an isolated container (Docker/Podman)
- Adds ~1-2s overhead per conversion for container lifecycle
- Best for: security-sensitive environments, multi-tenant deployments

## Benchmarking

### Load Testing with cURL

Simple sequential benchmark:

```bash
# Time a single conversion
time curl -s -X POST http://localhost:8080/api/convert \
  -F "file=@test-file.docx" \
  -o /dev/null -w "%{http_code} %{time_total}s\n"
```

### Load Testing the Batch Endpoint

```bash
# Batch of 5 files
time curl -s -X POST http://localhost:8080/api/convert/batch \
  -F "files=@file1.docx" \
  -F "files=@file2.xlsx" \
  -F "files=@file3.csv" \
  -F "files=@file4.html" \
  -F "files=@file5.md" \
  -o /dev/null -w "%{http_code} %{time_total}s\n"
```

### Concurrent Load Testing with Apache Bench

```bash
# Prepare a file for repeated upload
# 50 requests, 10 concurrent
ab -n 50 -c 10 -p test-file.docx \
  -T "multipart/form-data; boundary=----WebKitFormBoundary" \
  http://localhost:8080/api/convert
```

### Load Testing with wrk (HTTP benchmarking tool)

For more realistic load patterns, use wrk with a Lua script:

```lua
-- wrk-convert.lua
wrk.method = "POST"
wrk.headers["Content-Type"] = "multipart/form-data; boundary=----Boundary"

local file_content = io.open("test-file.docx", "rb"):read("*all")
wrk.body = "------Boundary\r\n"
  .. "Content-Disposition: form-data; name=\"file\"; filename=\"test.docx\"\r\n"
  .. "Content-Type: application/octet-stream\r\n\r\n"
  .. file_content
  .. "\r\n------Boundary--\r\n"
```

```bash
# 30 second test, 4 threads, 20 connections
wrk -t4 -c20 -d30s -s wrk-convert.lua http://localhost:8080/api/convert
```

### Async Endpoint Load Test

For testing throughput with the async endpoint:

```bash
#!/bin/bash
# Submit 20 async jobs and measure total time
START=$(date +%s)
JOBS=()

for i in $(seq 1 20); do
  RESPONSE=$(curl -s -X POST http://localhost:8080/api/convert/async \
    -F "file=@test-file.docx")
  JOB_ID=$(echo "$RESPONSE" | jq -r '.jobId')
  JOBS+=("$JOB_ID")
  echo "Submitted job $i: $JOB_ID"
done

# Wait for all jobs to complete
for JOB_ID in "${JOBS[@]}"; do
  while true; do
    STATUS=$(curl -s http://localhost:8080/api/convert/async/$JOB_ID | jq -r '.status')
    if [ "$STATUS" = "COMPLETED" ] || [ "$STATUS" = "FAILED" ]; then
      echo "Job $JOB_ID: $STATUS"
      break
    fi
    sleep 1
  done
done

END=$(date +%s)
echo "Total time: $((END - START)) seconds for ${#JOBS[@]} jobs"
```

### Key Metrics to Monitor

During load testing, monitor these metrics via the Prometheus endpoint (`/actuator/prometheus`):

| Metric | What to Watch |
|--------|--------------|
| `file_conversion_duration_seconds` | P50, P95, P99 latencies |
| `file_conversion_total` | Total conversion count |
| `file_conversion_errors_total` | Error rate by type |
| `jvm_memory_used_bytes` | Heap usage under load |
| `jvm_gc_pause_seconds` | GC pause frequency and duration |
| `system_cpu_usage` | CPU saturation |
| `jvm_threads_live_threads` | Thread count (carrier + virtual) |

### Baseline Performance Targets

For a healthy production deployment (4 cores, 8GB RAM):

| Metric | Target | Action if Exceeded |
|--------|--------|-------------------|
| P95 latency (small files) | < 2s | Check GC pressure, increase heap |
| P95 latency (medium files) | < 10s | Check concurrency limits |
| Error rate | < 1% | Check logs, resource limits |
| GC pause time (P99) | < 500ms | Switch to ZGC or increase heap |
| Heap usage (steady state) | < 70% | Increase heap or reduce cache size |
| CPU usage (sustained) | < 80% | Scale horizontally |

### Identifying Bottlenecks

1. **High GC pressure:** Frequent GC pauses, heap near capacity — increase heap or enable streaming for large files
2. **CPU saturation:** All cores at 100% — reduce `parallel-workers`, scale horizontally
3. **Thread pinning:** Virtual thread carrier pool exhausted — check for `synchronized` in hot paths, use `-Djdk.tracePinnedThreads=short`
4. **Disk I/O:** Slow temp file writes — use faster storage (SSD), check temp directory space
5. **Rate limiting:** Many 429 responses — increase rate limit or add API keys for trusted clients
