package com.xtopdf.xtopdf.adapters.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ExposedPort;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for container adapter behavior.
 *
 * Property 8: Disabled Adapter Local Execution
 * Property 9: Enabled Adapter Failure Propagation
 */
class ContainerAdapterPropertyTest {

    private static final ContainerConfig DEFAULT_CONFIG = ContainerConfig.builder()
            .imageName("xtopdf-converter:latest")
            .memoryLimit("512m")
            .cpuLimit(1)
            .timeoutSeconds(300)
            .cleanupEnabled(true)
            .containerPort(8080)
            .build();

    /**
     * Property 8: Disabled Adapter Local Execution
     *
     * For any Runnable passed to a Container_Adapter with orchestration disabled,
     * the adapter SHALL execute the Runnable directly (the Runnable's side effects
     * are observable) without creating any container.
     *
     * **Validates: Requirements 8.1**
     */
    @Property
    @Label("Property 8: Disabled adapter executes Runnable directly with observable side effects")
    void disabledAdapterExecutesRunnableDirectly(
            @ForAll("sideEffectIds") int sideEffectId) throws Exception {

        // Use DockerContainerAdapter with enabled=false
        DockerContainerAdapter adapter = new DockerContainerAdapter(DEFAULT_CONFIG, false);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        // Create a Runnable with an observable side effect
        AtomicInteger observedValue = new AtomicInteger(-1);
        Runnable runnable = () -> observedValue.set(sideEffectId);

        adapter.executeInContainer(inputFile, "/tmp/output.pdf", runnable);

        // The side effect must be observable
        assertThat(observedValue.get()).isEqualTo(sideEffectId);
    }

    /**
     * Property 8 (Podman variant): Disabled Adapter Local Execution
     *
     * Same property verified for PodmanContainerAdapter.
     *
     * **Validates: Requirements 8.1**
     */
    @Property
    @Label("Property 8: Disabled Podman adapter executes Runnable directly with observable side effects")
    void disabledPodmanAdapterExecutesRunnableDirectly(
            @ForAll("sideEffectIds") int sideEffectId) throws Exception {

        PodmanContainerAdapter adapter = new PodmanContainerAdapter(DEFAULT_CONFIG, false);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        AtomicInteger observedValue = new AtomicInteger(-1);
        Runnable runnable = () -> observedValue.set(sideEffectId);

        adapter.executeInContainer(inputFile, "/tmp/output.pdf", runnable);

        assertThat(observedValue.get()).isEqualTo(sideEffectId);
    }

    /**
     * Property 9: Enabled Adapter Failure Propagation
     *
     * For any container creation failure with an error message, the Container_Adapter
     * SHALL throw a FileConversionException whose message contains the failure reason string.
     *
     * **Validates: Requirements 8.2**
     */
    @Property
    @Label("Property 9: Enabled adapter propagates failure reason in FileConversionException")
    void enabledAdapterPropagatesFailureReason(
            @ForAll("errorMessages") String errorMessage) throws Exception {

        // Create a DockerContainerAdapter with enabled=true and mock the DockerClient
        DockerContainerAdapter adapter = new DockerContainerAdapter(DEFAULT_CONFIG, true);

        // Inject mocked DockerClient that throws on createContainerCmd
        DockerClient mockDockerClient = mock(DockerClient.class);
        when(mockDockerClient.createContainerCmd(anyString()))
                .thenThrow(new DockerException(errorMessage, 500));

        Field dockerClientField = DockerContainerAdapter.class.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        dockerClientField.set(adapter, mockDockerClient);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        try {
            adapter.executeInContainer(inputFile, "/tmp/output.pdf", () -> {});
            // Should not reach here
            assertThat(false).as("Expected FileConversionException to be thrown").isTrue();
        } catch (FileConversionException ex) {
            assertThat(ex.getMessage()).contains(errorMessage);
        }
    }

    // ---- Arbitraries ----

    @Provide
    Arbitrary<Integer> sideEffectIds() {
        return Arbitraries.integers().between(0, 100_000);
    }

    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(100)
                .map(s -> "Error: " + s);
    }
}
