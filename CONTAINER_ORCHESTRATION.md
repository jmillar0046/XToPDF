# Container Orchestration Feature

## Overview

XToPDF now supports running each file conversion in an isolated Docker container. This feature ensures that:
- Each conversion job is completely isolated from others
- Resources are allocated and cleaned up per job
- Failed conversions don't affect the main application or other jobs

## Configuration

Container orchestration is disabled by default. To enable it, configure the following properties in `application.properties`:

```properties
# Enable container orchestration
container.orchestration.enabled=true

# Docker image configuration
container.orchestration.image.name=xtopdf-converter
container.orchestration.image.tag=latest

# Resource limits
container.orchestration.memory.limit=512m
container.orchestration.cpu.limit=1

# Timeout settings
container.orchestration.timeout.seconds=300

# Cleanup settings
container.orchestration.cleanup.enabled=true
```

## Building the Docker Image

Before enabling container orchestration, you need to build the Docker image:

```bash
docker build -t xtopdf-converter:latest .
```

The Dockerfile is included in the repository root.

## How It Works

When container orchestration is enabled:

1. **Request arrives**: A file conversion request is received via the REST API
2. **Container created**: A new Docker container is spun up from the configured image
3. **Resources allocated**: Memory and CPU limits are applied to the container
4. **Conversion executed**: The file is sent to the container for conversion
5. **Result retrieved**: The converted PDF is retrieved from the container
6. **Container cleaned up**: The container is stopped and removed (if cleanup is enabled)

When container orchestration is disabled (default), conversions run in the main application process as before.

## Benefits

- **Isolation**: Each conversion runs in its own environment
- **Resource Control**: Memory and CPU limits prevent resource exhaustion
- **Security**: Conversions are sandboxed from the host system
- **Scalability**: Multiple conversions can run in parallel without interference
- **Fault Tolerance**: Failed conversions don't crash the main application

## Performance Considerations

Container orchestration adds overhead due to:
- Container startup time (~1-3 seconds)
- Network communication between host and container
- Container cleanup

For high-throughput scenarios, consider:
- Increasing `timeoutSeconds` for large files
- Adjusting memory and CPU limits based on your workload
- Using a container orchestration platform (like Kubernetes) for better resource management

## Testing

The container orchestration feature has comprehensive unit tests with 100% coverage on testable code. Integration tests with actual Docker containers can be run separately.

To test the feature:

```bash
# Run unit tests
./gradlew test

# Check code coverage
./gradlew jacocoTestReport
```

## Architecture

The implementation consists of:

1. **ContainerOrchestrationConfig**: Configuration properties for Docker container settings
2. **ContainerOrchestrationService**: Service that manages Docker container lifecycle
3. **FileConversionService**: Updated to optionally use container orchestration
4. **Dockerfile**: Docker image definition for the conversion service

## Troubleshooting

### Container fails to start
- Check if Docker is installed and running: `docker info`
- Verify the image exists: `docker images | grep xtopdf-converter`
- Check Docker logs: `docker logs <container-id>`

### Conversion times out
- Increase `container.orchestration.timeout.seconds`
- Check if the file is too large for the memory limit
- Verify network connectivity between host and container

### High resource usage
- Reduce `container.orchestration.cpu.limit`
- Reduce `container.orchestration.memory.limit`
- Enable cleanup to ensure containers are removed: `container.orchestration.cleanup.enabled=true`

## Future Enhancements

Potential improvements:
- Container pooling for faster response times
- Support for multiple Docker registries
- Integration with Kubernetes for production deployments
- Monitoring and metrics for container operations
- Support for GPU-accelerated conversions
- Dedicated health check endpoint for container readiness detection
- More robust port allocation strategy to handle concurrent container creation
- Extract memory parsing logic to a utility class for better testability
