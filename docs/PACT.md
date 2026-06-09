# Contract Testing with Pact

This document describes how to set up and use Pact contract testing for the XToPDF API.

## Overview

Contract testing verifies that the API provider (XToPDF) and its consumers agree on the API interface. Pact tests use a consumer-driven approach:

1. **Consumer** defines expected interactions (contracts)
2. **Provider** verifies it satisfies those contracts

## Running Contract Tests

### Consumer Tests

Generate pact files by running the consumer test:

```bash
./gradlew test --tests "com.xtopdf.xtopdf.contract.PactConsumerTest"
```

This creates pact files in `build/pacts/`.

### Provider Verification

Verify the provider meets the contract:

```bash
./gradlew test --tests "com.xtopdf.xtopdf.contract.PactProviderTest"
```

This starts the application and replays the consumer interactions against it.

## Contract: POST /api/convert

The primary contract defines the file conversion endpoint:

- **Method**: POST
- **Path**: `/api/convert`
- **Content-Type**: `multipart/form-data`
- **Request Body**: Multipart form with a `file` field
- **Expected Response**: HTTP 200 with `Content-Type: application/pdf`

## Pact Broker Setup

A Pact Broker provides a central repository for contracts and enables CI/CD integration.

### Option 1: PactFlow (SaaS)

1. Sign up at [pactflow.io](https://pactflow.io)
2. Get your broker URL and API token
3. Add to `build.gradle`:

```groovy
pact {
    broker {
        pactBrokerUrl = System.getenv('PACT_BROKER_URL') ?: 'https://your-org.pactflow.io'
        pactBrokerToken = System.getenv('PACT_BROKER_TOKEN')
    }
    publish {
        pactBrokerUrl = System.getenv('PACT_BROKER_URL') ?: 'https://your-org.pactflow.io'
        pactBrokerToken = System.getenv('PACT_BROKER_TOKEN')
        consumerVersion = project.version
    }
}
```

### Option 2: Self-Hosted Pact Broker

Run the Pact Broker using Docker Compose:

```yaml
# docker-compose-pact.yml
version: '3'
services:
  pact-broker:
    image: pactfoundation/pact-broker:latest
    ports:
      - "9292:9292"
    environment:
      PACT_BROKER_DATABASE_URL: "sqlite:///pact_broker.sqlite3"
      PACT_BROKER_BASIC_AUTH_USERNAME: admin
      PACT_BROKER_BASIC_AUTH_PASSWORD: admin
    volumes:
      - pact-data:/tmp/pact_broker

volumes:
  pact-data:
```

Start:

```bash
docker compose -f docker-compose-pact.yml up -d
```

The broker will be available at `http://localhost:9292`.

### Publishing Pacts to Broker

After generating pacts, publish them:

```bash
./gradlew pactPublish
```

Or via the Pact CLI:

```bash
pact-broker publish build/pacts \
  --broker-base-url=http://localhost:9292 \
  --consumer-app-version=$(git rev-parse --short HEAD) \
  --branch=$(git rev-parse --abbrev-ref HEAD)
```

### Provider Verification from Broker

Update the provider test to use the broker instead of local pact files:

```java
@Provider("xtopdf-provider")
@PactBroker(
    url = "${PACT_BROKER_URL:http://localhost:9292}",
    authentication = @PactBrokerAuth(token = "${PACT_BROKER_TOKEN:}")
)
class PactProviderTest {
    // ... same as before
}
```

### CI/CD Integration

Add to your GitHub Actions workflow:

```yaml
- name: Run Consumer Tests
  run: ./gradlew test --tests "*PactConsumerTest"

- name: Publish Pacts
  run: ./gradlew pactPublish
  env:
    PACT_BROKER_URL: ${{ secrets.PACT_BROKER_URL }}
    PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}

- name: Verify Provider
  run: ./gradlew test --tests "*PactProviderTest"
  env:
    PACT_BROKER_URL: ${{ secrets.PACT_BROKER_URL }}
    PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}

- name: Can I Deploy?
  run: |
    pact-broker can-i-deploy \
      --pacticipant=xtopdf-provider \
      --version=$(git rev-parse --short HEAD) \
      --to-environment=production
```

## Webhooks

Configure the Pact Broker to trigger CI builds when contracts change:

1. Go to Pact Broker UI > Settings > Webhooks
2. Add a webhook that triggers your provider build when a consumer publishes a new pact
3. This ensures the provider is verified against new consumer expectations before deployment
