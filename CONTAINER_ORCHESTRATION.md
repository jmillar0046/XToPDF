# Container Orchestration Feature

## Overview

XToPDF now supports running each file conversion in an isolated container using **hexagonal architecture** (ports and adapters pattern). This design allows you to easily switch between different container runtimes (Docker, Podman, etc.) without changing the core business logic.

### Features
- Each conversion job runs in complete isolation
- Resources are allocated and cleaned up per job
- Failed conversions don't affect the main application or other jobs
- **Runtime-agnostic**: Easily switch between Docker, Podman, or other runtimes
- **Decoupled architecture**: Container logic is separated from business logic

## Architecture

The implementation follows **hexagonal architecture** (also known as ports and adapters pattern):

```
Core Domain (FileConversionService)
        ↓
    Port (ContainerRuntimePort interface)
        ↓
    Adapters:
    - PodmanContainerAdapter (default)
    - DockerContainerAdapter
    - [Your custom adapter]
```

### Components

1. **Port (Interface)**: `ContainerRuntimePort` - Defines the contract for container operations
2. **Adapters (Implementations)**:
   - `DockerContainerAdapter` - Docker implementation using docker-java library
   - `PodmanContainerAdapter` - Podman implementation using CLI commands
3. **Configuration**: `ContainerRuntimeConfiguration` - Selects the appropriate adapter based on configuration

This architecture ensures that:
- The core application doesn't depend on any specific container runtime
- You can add new runtime adapters without changing existing code
- Testing is easier with mock implementations

## Container Runtime Options

### Podman (Default - Recommended)
Podman is a daemonless, rootless container engine that's Docker-compatible and is the default runtime.

**Pros:**
- Daemonless architecture (better security)
- Rootless containers (no elevated privileges needed)
- Docker CLI compatible (drop-in replacement)
- Apache License 2.0 (free and open source)
- Better security features

**Cons:**
- Less mature ecosystem than Docker
- Some Docker Compose features may not be fully supported

### Docker (Alternative)
Docker is a mature and widely used container runtime.

**Pros:**
- Mature ecosystem
- Excellent documentation
- Wide adoption

**Cons:**
- Requires daemon
- Requires root or docker group membership

### Other Options
The architecture supports adding adapters for:
- **containerd**: Low-level runtime, ideal for Kubernetes
- **CRI-O**: Kubernetes-optimized runtime
- Any other OCI-compliant runtime

## Configuration

### Basic Setup (Podman - Default)

```properties
# Enable container isolation with Podman (default)
container.orchestration.enabled=true
container.orchestration.runtime=podman
container.orchestration.image.name=xtopdf-converter
container.orchestration.image.tag=latest
container.orchestration.memory.limit=512m
container.orchestration.cpu.limit=1
container.orchestration.container.port=8080
container.orchestration.timeout.seconds=300
container.orchestration.cleanup.enabled=true
```

### Using Docker Instead of Podman

Simply change the runtime configuration:

```properties
# Enable container isolation with Docker
container.orchestration.enabled=true
container.orchestration.runtime=docker
container.orchestration.image.name=xtopdf-converter
container.orchestration.image.tag=latest
container.orchestration.memory.limit=512m
container.orchestration.cpu.limit=1
container.orchestration.container.port=8080
container.orchestration.timeout.seconds=300
container.orchestration.cleanup.enabled=true
```

**Note**: Ensure Podman is installed:
```bash
# Fedora/RHEL/CentOS
sudo dnf install podman

# Ubuntu/Debian
sudo apt-get install podman

# macOS
brew install podman
```

## Building the Container Image

### For Docker:
```bash
docker build -t xtopdf-converter:latest .
```

### For Podman:
```bash
podman build -t xtopdf-converter:latest .
```

The Dockerfile is compatible with both Docker and Podman.

## How It Works

When container orchestration is enabled:

1. **Request arrives**: A file conversion request is received via the REST API
2. **Adapter selected**: The configured runtime adapter (Docker/Podman) is used
3. **Container created**: A new container is spun up from the configured image
4. **Resources allocated**: Memory and CPU limits are applied to the container
5. **Conversion executed**: The file is sent to the container for conversion
6. **Result retrieved**: The converted PDF is retrieved from the container
7. **Container cleaned up**: The container is stopped and removed (if cleanup is enabled)

When container orchestration is disabled (default), conversions run in the main application process as before.

## Adding a Custom Runtime Adapter

To add support for a new container runtime:

1. **Create a new adapter class** implementing `ContainerRuntimePort`:

```java
@Slf4j
public class MyCustomContainerAdapter implements ContainerRuntimePort {
    private final ContainerConfig config;
    private final boolean enabled;
    
    public MyCustomContainerAdapter(ContainerConfig config, boolean enabled) {
        this.config = config;
        this.enabled = enabled;
        // Initialize your runtime client
    }
    
    @Override
    public void executeInContainer(MultipartFile inputFile, String outputFile, 
                                   Runnable converterLogic) throws FileConversionException {
        // Implement container execution logic
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String getRuntimeInfo() {
        // Return runtime information
    }
}
```

2. **Register the adapter** in `ContainerRuntimeConfiguration`:

```java
return switch (runtime) {
    case "podman" -> new PodmanContainerAdapter(containerConfig, config.isEnabled());
    case "docker" -> new DockerContainerAdapter(containerConfig, config.isEnabled());
    case "mycustom" -> new MyCustomContainerAdapter(containerConfig, config.isEnabled());
    default -> // ... fallback
};
```

3. **Configure** in `application.properties`:

```properties
container.orchestration.runtime=mycustom
```

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
- Check if your container runtime is installed and running:
    - For **Podman** (default): `podman info`
    - For **Docker**: `docker info`
- Verify the image exists:
    - For **Podman** (default): `podman images | grep xtopdf-converter`
    - For **Docker**: `docker images | grep xtopdf-converter`
- Check container logs:
    - For **Podman** (default): `podman logs <container-id>`
    - For **Docker**: `docker logs <container-id>`

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
