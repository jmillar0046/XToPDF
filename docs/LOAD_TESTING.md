# Load Testing

This document describes how to run load tests against the XToPDF API using Gatling.

## Overview

The load test simulation (`ConversionSimulation`) exercises the `/api/convert` endpoint under concurrent load:

- Ramps up 10 virtual users over 30 seconds
- Each user sends 5 conversion requests with pauses between requests
- Validates that responses are either 200 (success) or 429 (rate limited)
- Asserts max response time < 30s and > 50% success rate

## Prerequisites

1. **Java 11+** (Gatling requirement)
2. **Gatling Gradle Plugin** - add to `build.gradle`:

```groovy
plugins {
    id 'io.gatling.gradle' version '3.11.5.2'
}
```

3. **Gatling dependencies** (automatically managed by the plugin):

```groovy
gatling 'io.gatling.highcharts:gatling-charts-highcharts:3.11.5'
gatling 'io.gatling:gatling-test-framework:3.11.5'
```

## Running Load Tests

### 1. Start the Application

```bash
./gradlew bootRun
```

Wait until the application is fully started (check `http://localhost:8080/actuator/health`).

### 2. Run the Simulation

```bash
./gradlew gatlingRun
```

Or target a specific simulation:

```bash
./gradlew gatlingRun-com.xtopdf.loadtest.ConversionSimulation
```

### 3. View Results

After the simulation completes, Gatling generates an HTML report at:

```
build/reports/gatling/conversionsimulation-<timestamp>/index.html
```

Open it in a browser to view:

- Response time distribution
- Requests per second
- Active users over time
- Response time percentiles (P50, P75, P95, P99)

## Simulation Details

| Parameter | Value |
|-----------|-------|
| Users | 10 |
| Ramp-up | 30 seconds |
| Requests per user | 5 |
| Total requests | 50 |
| Valid status codes | 200, 429 |
| Max response time assertion | 30s |
| Min success rate assertion | 50% |

## Customizing the Simulation

Edit `src/gatling/scala/com/xtopdf/loadtest/ConversionSimulation.scala` to:

- **Change load profile**: Modify `rampUsers(10).during(30.seconds)` to test different patterns
- **Add more scenarios**: Test batch endpoint, async endpoint, or different file types
- **Adjust assertions**: Tighten or relax performance thresholds

### Example: Stress Test

```scala
setUp(
  convertScenario.inject(
    rampUsers(100).during(60.seconds)
  )
)
```

### Example: Soak Test

```scala
setUp(
  convertScenario.inject(
    constantUsersPerSec(5).during(10.minutes)
  )
)
```

## CI/CD Integration

Add to your GitHub Actions workflow:

```yaml
- name: Start application
  run: ./gradlew bootRun &
  
- name: Wait for startup
  run: |
    for i in {1..30}; do
      curl -s http://localhost:8080/actuator/health && break || sleep 2
    done

- name: Run load tests
  run: ./gradlew gatlingRun

- name: Upload Gatling report
  uses: actions/upload-artifact@v4
  with:
    name: gatling-report
    path: build/reports/gatling/
```

## Interpreting Results

| Metric | Good | Acceptable | Poor |
|--------|------|------------|------|
| P95 Response Time | < 5s | < 10s | > 10s |
| Error Rate | < 1% | < 5% | > 5% |
| Throughput | > 10 req/s | > 5 req/s | < 5 req/s |

Note: Rate limiting (429 responses) is expected behavior and not counted as errors in the simulation assertions.
