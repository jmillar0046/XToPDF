# Monitoring and Observability Guide

## Overview

This document describes the monitoring and observability features of the XToPDF application, including metrics to track, logging patterns, and troubleshooting procedures.

## Metrics to Track

### 1. Temporary File Metrics

**Metric Name**: `xtopdf.temp.files.created`  
**Type**: Counter  
**Description**: Total number of temporary files created during conversions  
**Labels**: 
- `service`: Service name (e.g., PageNumberService, WatermarkService)
- `operation`: Operation type (e.g., page_numbers, watermark, merge)

**Metric Name**: `xtopdf.temp.files.deleted`  
**Type**: Counter  
**Description**: Total number of temporary files successfully deleted  
**Labels**: Same as above

**Metric Name**: `xtopdf.temp.files.orphaned`  
**Type**: Counter  
**Description**: Number of temporary files that failed to delete  
**Labels**: Same as above

**Alert**: If `orphaned` count increases continuously, investigate disk space and cleanup logic.

### 2. Conversion Duration Metrics

**Metric Name**: `xtopdf.conversion.duration`  
**Type**: Histogram  
**Description**: Time taken to convert files (in milliseconds)  
**Labels**:
- `file_type`: Input file extension (e.g., csv, tsv, docx, xlsx)
- `file_size_bucket`: File size bucket (small: <1MB, medium: 1-10MB, large: 10-100MB)
- `streaming`: Whether streaming mode was used (true/false)
- `status`: Conversion status (success, failure)

**Percentiles to Track**: p50, p95, p99

### 3. Conversion Failure Metrics

**Metric Name**: `xtopdf.conversion.failures`  
**Type**: Counter  
**Description**: Total number of failed conversions  
**Labels**:
- `file_type`: Input file extension
- `error_type`: Type of error (conversion_error, io_error, internal_error)
- `error_code`: Specific error code from ErrorResponse

**Alert**: If failure rate exceeds 5%, investigate logs with correlation IDs.

### 4. Memory Usage Metrics

**Metric Name**: `xtopdf.conversion.memory.used`  
**Type**: Gauge  
**Description**: Memory used during conversion (in bytes)  
**Labels**:
- `file_type`: Input file extension
- `file_size`: Actual file size in bytes
- `streaming`: Whether streaming mode was used

**Alert**: If memory usage exceeds 3x file size, investigate memory leaks.

### 5. File Size Metrics

**Metric Name**: `xtopdf.conversion.file.size.input`  
**Type**: Histogram  
**Description**: Size of input files (in bytes)  
**Labels**: `file_type`

**Metric Name**: `xtopdf.conversion.file.size.output`  
**Type**: Histogram  
**Description**: Size of output PDF files (in bytes)  
**Labels**: `input_file_type`

## Logging Patterns

### Structured Logging Format

All log messages include:
- **Timestamp**: ISO 8601 format
- **Level**: DEBUG, INFO, WARN, ERROR
- **Logger**: Fully qualified class name
- **Correlation ID**: UUID for tracking requests (when available)
- **Message**: Descriptive message with context
- **Exception**: Full stack trace for errors

### Log Levels

#### DEBUG
- Conversion start with file details
- Parsing progress (rows processed, columns detected)
- Streaming mode selection
- Chunk processing progress

Example:
```
2024-01-15 10:30:45.123 DEBUG [TsvToPdfService] Starting TSV to PDF conversion for file: data.tsv
2024-01-15 10:30:45.234 DEBUG [TsvToPdfService] Using streaming mode for large file: 15000000 bytes
2024-01-15 10:30:45.345 DEBUG [TsvToPdfService] Processed chunk of 1000 rows (total: 1000)
```

#### INFO
- Successful conversion completion
- File sizes and row counts
- Performance metrics

Example:
```
2024-01-15 10:30:50.456 INFO [TsvToPdfService] Successfully converted TSV to PDF using streaming: data.tsv -> data.pdf (15000 rows)
```

#### WARN
- Recoverable issues (empty files, malformed data)
- Validation warnings (line length, field count)
- Temporary file cleanup failures
- Unclosed quotes in CSV/TSV

Example:
```
2024-01-15 10:30:45.567 WARN [TsvToPdfService] Unclosed quote in line 42, treating as literal
2024-01-15 10:30:50.678 WARN [PageNumberService] Could not delete temporary file: /tmp/temp_page_numbers_12345.pdf
```

#### ERROR
- Conversion failures with full context
- I/O errors with file names
- Unexpected exceptions with stack traces
- Correlation IDs for tracking

Example:
```
2024-01-15 10:30:50.789 ERROR [GlobalExceptionHandler] File conversion error [correlationId=a1b2c3d4-e5f6-7890-abcd-ef1234567890]: Line 100 exceeds maximum length: 1000000
```

### Correlation ID Tracking

Correlation IDs are generated for:
1. **Error responses**: UUID generated in GlobalExceptionHandler
2. **Request tracking**: Can be added via MDC (Mapped Diagnostic Context)

To add correlation IDs to all logs:
```java
// In a filter or interceptor
String correlationId = UUID.randomUUID().toString();
MDC.put("correlationId", correlationId);
try {
    // Process request
} finally {
    MDC.remove("correlationId");
}
```

## Performance Characteristics

### File Size Limits

| Limit | Value | Configurable |
|-------|-------|--------------|
| Maximum file size | 100 MB | Yes (MAX_FILE_SIZE) |
| Maximum line length | 1 MB | Yes (MAX_LINE_LENGTH) |
| Maximum fields per row | 10,000 | Yes (MAX_FIELDS) |
| Streaming threshold | 10 MB | Yes (STREAMING_THRESHOLD) |
| Chunk size | 1,000 rows | Yes (CHUNK_SIZE) |

### Memory Usage

- **Small files (<10MB)**: ~2x file size in memory
- **Large files (>10MB)**: ~1.5x file size in memory (streaming mode)
- **Maximum memory**: Should not exceed 3x file size

### Conversion Times (Approximate)

| File Size | Format | In-Memory | Streaming |
|-----------|--------|-----------|-----------|
| 1 MB | CSV/TSV | ~500ms | ~600ms |
| 10 MB | CSV/TSV | ~2s | ~2.5s |
| 50 MB | CSV/TSV | N/A | ~10s |
| 100 MB | CSV/TSV | N/A | ~20s |

*Note: Times vary based on hardware and data complexity*

## Alerts and Thresholds

### Critical Alerts

1. **Disk Space Low**
   - Trigger: Available disk space < 10%
   - Action: Clean up temporary files, investigate orphaned files

2. **High Failure Rate**
   - Trigger: Conversion failure rate > 5% over 5 minutes
   - Action: Check logs with correlation IDs, investigate common errors

3. **Memory Leak**
   - Trigger: Memory usage continuously increasing
   - Action: Check for unclosed resources, review temporary file cleanup

4. **Orphaned Temp Files**
   - Trigger: `temp.files.orphaned` counter increasing
   - Action: Review cleanup logic, check file permissions

### Warning Alerts

1. **Slow Conversions**
   - Trigger: p95 conversion duration > 30s
   - Action: Review file sizes, check system resources

2. **High Memory Usage**
   - Trigger: Memory usage > 2.5x file size
   - Action: Verify streaming mode is enabled for large files

3. **Validation Warnings**
   - Trigger: High rate of validation warnings (>10% of conversions)
   - Action: Review input data quality, adjust limits if needed

## Troubleshooting Procedures

### Issue: Conversion Failures

1. **Find the correlation ID** from the error response
2. **Search logs** for that correlation ID:
   ```bash
   grep "correlationId=a1b2c3d4-e5f6-7890-abcd-ef1234567890" application.log
   ```
3. **Review the full stack trace** and error context
4. **Check common causes**:
   - File size exceeds limits
   - Malformed input data
   - Disk space issues
   - Permission problems

### Issue: Orphaned Temporary Files

1. **Check temp file metrics**:
   ```
   xtopdf.temp.files.created - xtopdf.temp.files.deleted = orphaned count
   ```
2. **Search logs for cleanup warnings**:
   ```bash
   grep "Could not delete temporary file" application.log
   ```
3. **Manually clean up** if needed:
   ```bash
   find /tmp -name "temp_*" -mtime +1 -delete
   ```
4. **Investigate root cause**: File permissions, disk full, process crashes

### Issue: High Memory Usage

1. **Check if streaming is enabled** for large files
2. **Verify STREAMING_THRESHOLD** is set correctly (default: 10MB)
3. **Review memory metrics** by file type and size
4. **Check for memory leaks**: Unclosed streams, retained references

### Issue: Slow Conversions

1. **Check file size** and complexity (rows, columns)
2. **Verify streaming mode** is used for large files
3. **Review system resources**: CPU, memory, disk I/O
4. **Check conversion duration metrics** by file type
5. **Consider optimization**: Increase CHUNK_SIZE, adjust limits

## Dashboard Recommendations

### Key Metrics Dashboard

1. **Conversion Rate**
   - Total conversions per minute
   - Success vs. failure rate
   - Breakdown by file type

2. **Performance**
   - p50, p95, p99 conversion duration
   - Memory usage trends
   - File size distribution

3. **Errors**
   - Error rate over time
   - Error types breakdown
   - Top error messages

4. **Resources**
   - Temporary file count
   - Orphaned file count
   - Disk space usage

### Example Grafana Queries

```promql
# Conversion rate
rate(xtopdf_conversion_total[5m])

# Failure rate
rate(xtopdf_conversion_failures_total[5m]) / rate(xtopdf_conversion_total[5m])

# p95 conversion duration
histogram_quantile(0.95, rate(xtopdf_conversion_duration_bucket[5m]))

# Orphaned files
xtopdf_temp_files_created_total - xtopdf_temp_files_deleted_total
```

## Log Aggregation

### Recommended Tools

- **ELK Stack**: Elasticsearch, Logstash, Kibana
- **Splunk**: Enterprise log management
- **Datadog**: Cloud-based monitoring
- **Grafana Loki**: Lightweight log aggregation

### Key Searches

1. **All errors for a file**:
   ```
   filename:"data.csv" AND level:ERROR
   ```

2. **Errors by correlation ID**:
   ```
   correlationId:"a1b2c3d4-e5f6-7890-abcd-ef1234567890"
   ```

3. **Slow conversions**:
   ```
   "Successfully converted" AND duration:>30000
   ```

4. **Validation warnings**:
   ```
   level:WARN AND ("exceeds maximum" OR "Unclosed quote")
   ```

## Best Practices

1. **Always include correlation IDs** in error responses
2. **Log file names and sizes** for context
3. **Use structured logging** for easy parsing
4. **Set up alerts** for critical metrics
5. **Review logs regularly** for patterns
6. **Monitor disk space** for temporary files
7. **Track memory usage** to detect leaks
8. **Benchmark performance** after changes
9. **Document common errors** and solutions
10. **Keep metrics retention** for trend analysis

## Future Enhancements

- [ ] Add distributed tracing with OpenTelemetry
- [ ] Implement custom metrics with Micrometer
- [ ] Add health check endpoints
- [ ] Create automated alerting rules
- [ ] Implement log sampling for high-volume scenarios
- [ ] Add performance profiling hooks
- [ ] Create real-time monitoring dashboard
- [ ] Implement anomaly detection for metrics
