# Chaos Testing

This document describes how to run chaos tests against the XToPDF service using Chaos Monkey for Spring Boot.

## Overview

Chaos Monkey for Spring Boot introduces controlled failures into the application to verify resilience:

- **Latency assault**: Adds 100-500ms random delay to service method calls
- **Exception assault**: Throws RuntimeExceptions with ~5% probability (level 5 = 1 in 5 chance per assault check)
- **Watched services**: `FileConversionService` (the core conversion orchestrator)

## Prerequisites

The `de.codecentric:chaos-monkey-spring-boot:3.1.0` dependency is already included in `build.gradle`.

The chaos profile is defined in `src/main/resources/application-chaos.properties`.

## Running Chaos Tests

### 1. Start with Chaos Profile

```bash
./gradlew bootRun --args='--spring.profiles.active=chaos'
```

Or via JAR:

```bash
java -jar build/libs/xtopdf-*.jar --spring.profiles.active=chaos
```

### 2. Verify Chaos Monkey is Active

```bash
curl http://localhost:8080/actuator/chaosmonkey/status
```

Expected: `"You switched me on!"`

### 3. Send Test Requests

```bash
# Send multiple conversion requests to observe chaos effects
for i in $(seq 1 20); do
  echo "Request $i:"
  curl -s -o /dev/null -w "HTTP %{http_code} - %{time_total}s\n" \
    -X POST http://localhost:8080/api/convert \
    -F "file=@testfile.txt"
done
```

You should observe:
- Some requests taking 100-500ms longer than normal (latency assault)
- Some requests returning 500 errors (exception assault)
- The application remaining responsive overall

## Configuration Details

| Property | Value | Description |
|----------|-------|-------------|
| `chaos.monkey.enabled` | `true` | Master switch |
| `chaos.monkey.watcher.service` | `true` | Watch @Service beans |
| `chaos.monkey.assaults.latencyActive` | `true` | Enable latency injection |
| `chaos.monkey.assaults.latencyRangeStart` | `100` | Min delay (ms) |
| `chaos.monkey.assaults.latencyRangeEnd` | `500` | Max delay (ms) |
| `chaos.monkey.assaults.exceptionsActive` | `true` | Enable exception throwing |
| `chaos.monkey.assaults.level` | `5` | 1-in-5 probability per call |

## Runtime Control

Chaos Monkey exposes a management endpoint for runtime configuration changes.

### Disable All Assaults

```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/disable
```

### Enable Assaults

```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/enable
```

### Change Assault Configuration at Runtime

```bash
curl -X POST http://localhost:8080/actuator/chaosmonkey/assaults \
  -H "Content-Type: application/json" \
  -d '{
    "latencyActive": true,
    "latencyRangeStart": 200,
    "latencyRangeEnd": 1000,
    "exceptionsActive": false,
    "level": 3
  }'
```

### View Current Configuration

```bash
curl http://localhost:8080/actuator/chaosmonkey/assaults
curl http://localhost:8080/actuator/chaosmonkey/watchers
```

## What to Verify

When running chaos tests, verify:

1. **Graceful degradation**: The application returns proper error responses (not stack traces)
2. **No data corruption**: Successful conversions still produce valid PDFs
3. **Resource cleanup**: Temp files are cleaned up even when exceptions occur
4. **Monitoring**: Errors and latency are properly reflected in Prometheus metrics
5. **Recovery**: The service recovers normally after assault stops

## Integration with Load Tests

For comprehensive resilience testing, combine chaos testing with load tests:

```bash
# Terminal 1: Start app with chaos profile
./gradlew bootRun --args='--spring.profiles.active=chaos'

# Terminal 2: Run Gatling load test
./gradlew gatlingRun
```

This reveals how the system behaves under load when failures are injected.

## Safety Notes

- **Never enable the chaos profile in production** - it is for testing environments only
- The `killApplicationActive` and `memoryActive` assaults are disabled by default for safety
- The chaos profile is only activated when explicitly specified via `--spring.profiles.active=chaos`
- Without the chaos profile, Chaos Monkey has no effect on the application
