package com.xtopdf.xtopdf.adapters.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Info;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DockerContainerAdapter.
 *
 * Tests cover:
 * - Disabled orchestration: local Runnable execution
 * - Enabled + container creation failure: FileConversionException with failure reason
 * - Enabled + container readiness timeout: FileConversionException indicating timeout
 * - Cleanup attempted after conversion when cleanup enabled
 * - Cleanup failure logs warning and does not throw
 * - parseMemoryLimit() for "512m", "1g", "1024k" formats
 *
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6
 */
class DockerContainerAdapterTest {

    @TempDir
    Path tempDir;

    private ContainerConfig config;
    private DockerClient mockDockerClient;
    private RestTemplate mockRestTemplate;

    @BeforeEach
    void setUp() {
        config = ContainerConfig.builder()
                .imageName("xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        mockDockerClient = mock(DockerClient.class);
        mockRestTemplate = mock(RestTemplate.class);
    }

    // ---- Requirement 8.1: Disabled orchestration executes Runnable directly ----

    /**
     * When disabled, executeInContainer() runs the Runnable directly without creating a container.
     * Validates: Requirement 8.1
     */
    @Test
    void whenDisabled_executeInContainer_runsRunnableDirectly() throws Exception {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        AtomicBoolean executed = new AtomicBoolean(false);
        adapter.executeInContainer(inputFile, outputFile, () -> executed.set(true));

        assertTrue(executed.get(), "Runnable should have been executed directly");
        assertFalse(adapter.isEnabled());
    }

    // ---- Requirement 8.2: Enabled + container creation failure ----

    /**
     * When enabled and container creation fails, a FileConversionException is thrown
     * with the failure reason.
     * Validates: Requirement 8.2
     */
    @Test
    void whenEnabled_containerCreationFails_throwsFileConversionExceptionWithReason() throws Exception {
        DockerContainerAdapter adapter = createAdapterWithMocks(true);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        when(mockDockerClient.createContainerCmd(anyString()))
                .thenThrow(new DockerException("Docker daemon not running", 500));

        FileConversionException ex = assertThrows(FileConversionException.class,
                () -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        assertThat(ex.getMessage()).contains("Docker daemon not running");
        assertThat(ex.getMessage()).contains("Failed to execute conversion in Docker container");
    }

    // ---- Requirement 8.3: Container readiness timeout ----

    /**
     * When the container fails to become ready within timeout, a FileConversionException
     * is thrown indicating timeout.
     * Validates: Requirement 8.3
     */
    @Test
    void whenEnabled_containerNeverReady_throwsTimeoutException() throws Exception {
        DockerContainerAdapter adapter = createAdapterWithMocks(true);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        CreateContainerCmd createCmd = mockCreateContainerCmd("container-timeout");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-timeout")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-timeout")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-timeout")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);

        // Container never becomes ready
        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        FileConversionException ex = assertThrows(FileConversionException.class,
                () -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        assertThat(ex.getMessage()).contains("failed to become ready");
    }

    // ---- Requirement 8.4: Cleanup attempted after conversion ----

    /**
     * Cleanup is attempted after conversion completes when cleanup is enabled.
     * Validates: Requirement 8.4
     */
    @Test
    void whenCleanupEnabled_cleanupIsAttemptedAfterConversion() throws Exception {
        DockerContainerAdapter adapter = createAdapterWithMocks(true);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        CreateContainerCmd createCmd = mockCreateContainerCmd("container-cleanup");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-cleanup")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-cleanup")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-cleanup")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);

        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        adapter.executeInContainer(inputFile, outputFile, () -> {});

        verify(mockDockerClient).stopContainerCmd("container-cleanup");
        verify(mockDockerClient).removeContainerCmd("container-cleanup");
    }

    /**
     * Cleanup is also attempted when conversion fails (finally block).
     * Validates: Requirement 8.4
     */
    @Test
    void whenCleanupEnabled_cleanupIsAttemptedEvenOnFailure() throws Exception {
        DockerContainerAdapter adapter = createAdapterWithMocks(true);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        CreateContainerCmd createCmd = mockCreateContainerCmd("container-fail");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-fail")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-fail")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-fail")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);

        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Conversion failed"));

        assertThrows(FileConversionException.class,
                () -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        verify(mockDockerClient).stopContainerCmd("container-fail");
        verify(mockDockerClient).removeContainerCmd("container-fail");
    }

    /**
     * When cleanup is disabled, stop/remove are NOT called.
     * Validates: Requirement 8.4
     */
    @Test
    void whenCleanupDisabled_noCleanupAttempted() throws Exception {
        ContainerConfig noCleanupConfig = ContainerConfig.builder()
                .imageName("xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(false)
                .containerPort(8080)
                .build();

        DockerContainerAdapter adapter = createAdapterWithMocks(noCleanupConfig, true);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        CreateContainerCmd createCmd = mockCreateContainerCmd("container-nocleanup");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-nocleanup")).thenReturn(startCmd);

        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        adapter.executeInContainer(inputFile, outputFile, () -> {});

        verify(mockDockerClient, never()).stopContainerCmd(anyString());
        verify(mockDockerClient, never()).removeContainerCmd(anyString());
    }

    // ---- Requirement 8.5: Cleanup failure logs warning, does not throw ----

    /**
     * Cleanup failure logs a warning and does not throw.
     * Validates: Requirement 8.5
     */
    @Test
    void whenCleanupFails_doesNotThrow() throws Exception {
        DockerContainerAdapter adapter = createAdapterWithMocks(true);

        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        CreateContainerCmd createCmd = mockCreateContainerCmd("container-cleanupfail");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-cleanupfail")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-cleanupfail")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-cleanupfail")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);
        doThrow(new DockerException("Failed to remove container", 500))
                .when(removeCmd).exec();

        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // Should not throw despite cleanup failure
        assertDoesNotThrow(() -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        verify(mockDockerClient).removeContainerCmd("container-cleanupfail");
    }

    // ---- Requirement 8.6: parseMemoryLimit ----

    /**
     * parseMemoryLimit("512m") returns 512 * 1024 * 1024.
     * Validates: Requirement 8.6
     */
    @Test
    void parseMemoryLimit_512m_returnsCorrectBytes() {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        long result = adapter.parseMemoryLimit("512m");
        assertEquals(512L * 1024L * 1024L, result);
    }

    /**
     * parseMemoryLimit("1g") returns 1 * 1024^3.
     * Validates: Requirement 8.6
     */
    @Test
    void parseMemoryLimit_1g_returnsCorrectBytes() {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        long result = adapter.parseMemoryLimit("1g");
        assertEquals(1L * 1024L * 1024L * 1024L, result);
    }

    /**
     * parseMemoryLimit("1024k") returns 1024 * 1024.
     * Validates: Requirement 8.6
     */
    @Test
    void parseMemoryLimit_1024k_returnsCorrectBytes() {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        long result = adapter.parseMemoryLimit("1024k");
        assertEquals(1024L * 1024L, result);
    }

    /**
     * parseMemoryLimit handles uppercase units.
     * Validates: Requirement 8.6
     */
    @Test
    void parseMemoryLimit_uppercaseUnit_returnsCorrectBytes() {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        assertEquals(256L * 1024L * 1024L, adapter.parseMemoryLimit("256M"));
        assertEquals(2L * 1024L * 1024L * 1024L, adapter.parseMemoryLimit("2G"));
        assertEquals(512L * 1024L, adapter.parseMemoryLimit("512K"));
    }

    /**
     * parseMemoryLimit with invalid format returns default 512MB.
     * Validates: Requirement 8.6
     */
    @Test
    void parseMemoryLimit_invalidFormat_returnsDefault512MB() {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        long defaultValue = 512L * 1024L * 1024L;
        assertEquals(defaultValue, adapter.parseMemoryLimit("invalid"));
    }

    // ---- Helper methods ----

    private DockerContainerAdapter createAdapterWithMocks(boolean enabled) throws Exception {
        return createAdapterWithMocks(config, enabled);
    }

    private DockerContainerAdapter createAdapterWithMocks(ContainerConfig cfg, boolean enabled) throws Exception {
        DockerContainerAdapter adapter = new DockerContainerAdapter(cfg, enabled);

        if (enabled) {
            Field dockerClientField = DockerContainerAdapter.class.getDeclaredField("dockerClient");
            dockerClientField.setAccessible(true);
            dockerClientField.set(adapter, mockDockerClient);

            Field restTemplateField = DockerContainerAdapter.class.getDeclaredField("restTemplate");
            restTemplateField.setAccessible(true);
            restTemplateField.set(adapter, mockRestTemplate);
        }

        return adapter;
    }

    private CreateContainerCmd mockCreateContainerCmd(String containerId) {
        CreateContainerCmd createCmd = mock(CreateContainerCmd.class);
        CreateContainerResponse response = mock(CreateContainerResponse.class);

        when(createCmd.withHostConfig(any())).thenReturn(createCmd);
        when(createCmd.withExposedPorts(any(ExposedPort.class))).thenReturn(createCmd);
        when(createCmd.exec()).thenReturn(response);
        when(response.getId()).thenReturn(containerId);

        return createCmd;
    }
}
