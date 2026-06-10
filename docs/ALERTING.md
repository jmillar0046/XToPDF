# Alerting Configuration

This document describes how to integrate XToPDF's Prometheus alerting rules with Alertmanager for production monitoring.

## Alerting Rules

The alerting rules are defined in `deploy/prometheus/alerting-rules.yml` and cover:

| Alert | Condition | Severity |
|-------|-----------|----------|
| HighConversionErrorRate | >5% conversions failing over 5 minutes | Critical |
| HighConversionLatency | P95 conversion time >10s over 5 minutes | Warning |
| HighMemoryUsage | >85% JVM heap for 5 minutes | Warning |
| RateLimitSaturation | >50% of requests hitting 429 over 5 minutes | Warning |

## Integrating with Alertmanager

### 1. Add Alerting Rules to Prometheus

In your `prometheus.yml`, add the rules file:

```yaml
rule_files:
  - "alerting-rules.yml"
```

If running Prometheus via Docker, mount the rules file:

```bash
docker run -d \
  -v ./deploy/prometheus/alerting-rules.yml:/etc/prometheus/alerting-rules.yml \
  -v ./prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

### 2. Configure Alertmanager

Create an `alertmanager.yml` configuration:

```yaml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'default'
  routes:
    - match:
        severity: critical
      receiver: 'pagerduty-critical'
    - match:
        severity: warning
      receiver: 'slack-warnings'

receivers:
  - name: 'default'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#xtopdf-alerts'
        title: '{{ .GroupLabels.alertname }}'
        text: '{{ .CommonAnnotations.description }}'

  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_SERVICE_KEY'
        description: '{{ .CommonAnnotations.summary }}'
        details:
          description: '{{ .CommonAnnotations.description }}'
          runbook: '{{ .CommonAnnotations.runbook_url }}'

  - name: 'slack-warnings'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK'
        channel: '#xtopdf-alerts'
        title: '[{{ .Status | toUpper }}] {{ .GroupLabels.alertname }}'
        text: '{{ .CommonAnnotations.description }}'
```

### 3. Connect Prometheus to Alertmanager

Add to your `prometheus.yml`:

```yaml
alerting:
  alertmanagers:
    - static_configs:
        - targets:
            - alertmanager:9093
```

### 4. Running Alertmanager

```bash
docker run -d \
  -p 9093:9093 \
  -v ./alertmanager.yml:/etc/alertmanager/alertmanager.yml \
  prom/alertmanager
```

## Slack Integration

For Slack notifications:

1. Create an Incoming Webhook in your Slack workspace (Apps > Incoming Webhooks)
2. Copy the webhook URL into the `api_url` field in `alertmanager.yml`
3. Customize the channel and message format as needed

## PagerDuty Integration

For PagerDuty notifications:

1. Create a new Service in PagerDuty
2. Add a Prometheus integration to the service
3. Copy the integration key into the `service_key` field in `alertmanager.yml`

## Runbooks

Each alert includes a `runbook_url` annotation pointing to detailed remediation steps. Create runbooks at the referenced paths covering:

- **High Error Rate**: Check application logs, verify dependent services, review recent deployments
- **High Latency**: Check file sizes being processed, review concurrent load, check disk I/O
- **High Memory**: Check for memory leaks, review heap dumps, consider scaling horizontally
- **Rate Limit Saturation**: Identify abusive clients, consider adjusting rate limits, check for DDoS

## Testing Alerts

To test that alerts fire correctly:

```bash
# Simulate high error rate by sending invalid files
for i in $(seq 1 100); do
  curl -X POST http://localhost:8080/api/v1/convert \
    -F "file=@/dev/null;filename=bad.xyz"
done

# Check Prometheus alerts page
open http://localhost:9090/alerts

# Check Alertmanager
open http://localhost:9093
```
