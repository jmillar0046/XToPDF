# ADR-0001: Use Hexagonal Architecture for Container Orchestration

## Status

Accepted

## Date

2025-01-15

## Context

XToPDF needs to support multiple container runtimes (Docker, Podman) for isolated file conversion. The orchestration layer must be decoupled from specific runtime implementations so that:

- New container runtimes can be added without modifying core business logic
- Testing is straightforward with mock implementations
- The system can fall back to local processing when no container runtime is available

Traditional layered architectures tightly couple business logic to infrastructure, making it difficult to swap container runtimes or test in isolation.

## Decision

We adopt hexagonal architecture (ports and adapters) for the container orchestration subsystem:

- **Port:** `ContainerRuntimePort` interface defines the contract for container operations (start, execute, stop, cleanup, health check)
- **Adapters:** `DockerContainerAdapter` and `PodmanContainerAdapter` implement the port for their respective runtimes
- **Configuration:** `ContainerOrchestrationConfig` selects the active adapter based on `container.orchestration.runtime` property
- **Domain:** `FileConversionService` depends only on the port, never on concrete adapters

```
FileConversionService → ContainerRuntimePort (port)
                              ↑
              ┌───────────────┼───────────────┐
              │               │               │
   DockerContainerAdapter  PodmanContainerAdapter  (future adapters)
```

## Consequences

### Positive

- Container runtime can be swapped via configuration without code changes
- Unit tests mock the port interface — no Docker/Podman needed in CI
- Clear separation of concerns: business logic vs infrastructure
- Easy to add new runtimes (Kubernetes, containerd) by implementing the port
- Adapter-specific error handling is encapsulated

### Negative

- Additional indirection layer increases code complexity slightly
- Developers must understand the port/adapter pattern
- Configuration-based wiring requires integration testing to verify

### Neutral

- ContainerConfig is an immutable Java record used across all adapters
- Health checks use the same port abstraction

## Alternatives Considered

| Alternative | Pros | Cons | Reason for Rejection |
|-------------|------|------|------|
| Direct Docker client usage | Simpler, fewer classes | Tight coupling, hard to test, no Podman support | Cannot swap runtimes without code changes |
| Strategy pattern only | Less abstraction | No clear boundary between domain and infra | Doesn't enforce dependency inversion |
| Plugin system (ServiceLoader) | Runtime extensibility | Over-engineered for 2-3 adapters, complex classloading | Too complex for current needs |

## References

- Alistair Cockburn, "Hexagonal Architecture" (2005)
- `src/main/java/com/xtopdf/xtopdf/ports/ContainerRuntimePort.java`
- `src/main/java/com/xtopdf/xtopdf/adapters/container/DockerContainerAdapter.java`
- `src/main/java/com/xtopdf/xtopdf/adapters/container/PodmanContainerAdapter.java`
- `src/main/java/com/xtopdf/xtopdf/config/ContainerOrchestrationConfig.java`
