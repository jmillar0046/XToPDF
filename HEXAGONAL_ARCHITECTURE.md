# Hexagonal Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     XToPDF Application                          │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │            Core Domain / Business Logic                   │ │
│  │                                                           │ │
│  │  ┌────────────────────────────────────────┐              │ │
│  │  │    FileConversionService               │              │ │
│  │  │  - convertFile()                       │              │ │
│  │  │  - Orchestrates conversion pipeline    │              │ │
│  │  └───────────────┬────────────────────────┘              │ │
│  │                  │                                        │ │
│  │                  │ uses                                   │ │
│  │                  ↓                                        │ │
│  │  ┌────────────────────────────────────────┐              │ │
│  │  │  ContainerOrchestrationService         │              │ │
│  │  │  - executeInContainer()                │              │ │
│  │  │  - isEnabled()                         │              │ │
│  │  └───────────────┬────────────────────────┘              │ │
│  │                  │                                        │ │
│  └──────────────────┼────────────────────────────────────────┘ │
│                     │                                          │
│                     │ depends on (port)                        │
│                     ↓                                          │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              PORT (Interface)                           │  │
│  │                                                         │  │
│  │  ┌──────────────────────────────────────────────────┐  │  │
│  │  │      ContainerRuntimePort                        │  │  │
│  │  │  + executeInContainer()                          │  │  │
│  │  │  + isEnabled()                                   │  │  │
│  │  │  + getRuntimeInfo()                              │  │  │
│  │  └──────────────────────────────────────────────────┘  │  │
│  └─────────────────────────────────────────────────────────┘  │
│                     ↑                                          │
│                     │ implemented by (adapters)                │
│                     │                                          │
│  ┌──────────────────┴──────────────────────────────────────┐  │
│  │             ADAPTERS (Implementations)                   │  │
│  │                                                          │  │
│  │  ┌──────────────────────┐  ┌───────────────────────┐   │  │
│  │  │ DockerContainer      │  │ PodmanContainer       │   │  │
│  │  │ Adapter              │  │ Adapter               │   │  │
│  │  │                      │  │                       │   │  │
│  │  │ - Uses docker-java   │  │ - Uses Podman CLI     │   │  │
│  │  │ - Creates containers │  │ - Creates containers  │   │  │
│  │  │ - Manages lifecycle  │  │ - Manages lifecycle   │   │  │
│  │  └──────────┬───────────┘  └───────────┬───────────┘   │  │
│  │             │                           │               │  │
│  └─────────────┼───────────────────────────┼───────────────┘  │
│                │                           │                  │
└────────────────┼───────────────────────────┼──────────────────┘
                 │                           │
                 ↓                           ↓
         ┌───────────────┐          ┌──────────────┐
         │     Docker    │          │    Podman    │
         │   (daemon)    │          │ (daemonless) │
         └───────────────┘          └──────────────┘
```

## Key Concepts

### Port (Interface)
- **ContainerRuntimePort**: Defines the contract for container operations
- Decouples business logic from specific container implementations
- Makes the system runtime-agnostic

### Adapters (Implementations)
- **DockerContainerAdapter**: Implements port using Docker Java API
- **PodmanContainerAdapter**: Implements port using Podman CLI
- Future adapters can be added (containerd, CRI-O, etc.)

### Configuration
```java
@Bean
public ContainerRuntimePort containerRuntimePort(config) {
    return switch (config.getRuntime()) {
        case "docker" -> new DockerContainerAdapter(...);
        case "podman" -> new PodmanContainerAdapter(...);
        default -> new DockerContainerAdapter(...);
    };
}
```

## Benefits of Hexagonal Architecture

1. **Separation of Concerns**: Business logic is isolated from infrastructure
2. **Testability**: Easy to mock ports for testing
3. **Flexibility**: Switch implementations via configuration
4. **Maintainability**: Changes to adapters don't affect core logic
5. **Extensibility**: Add new adapters without modifying existing code

## Runtime Selection

Switch between runtimes by changing configuration:

```properties
# Use Docker (default)
container.orchestration.runtime=docker

# Use Podman
container.orchestration.runtime=podman
```

No code changes required!
