package com.xtopdf.xtopdf.adapters.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for DockerContainerAdapter.
 * 
 * Tests cover:
 * - Container lifecycle: start, execute, stop, cleanup
 * - Error handling when Docker is unavailable
 * - Cleanup occurs even on failure
 * - Disabled orchestration fallback
 * 
 * Requirements: 1.1, 1.3, 1.4
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

    /**
     * Test: Container lifecycle - successful execution
     * Validates: Requirements 1.1, 1.3
     */
    @Test
    void testExecuteInContainer_Success() throws Exception {
        // Given: A properly configured adapter with mocked Docker client
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        // Mock Docker commands
        CreateContainerCmd createCmd = mockCreateContainerCmd("container-123");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-123")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-123")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-123")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);

        // Mock REST template for container readiness and conversion
        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // When: Execute conversion in container
        assertDoesNotThrow(() -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        // Then: Verify container lifecycle
        verify(mockDockerClient).createContainerCmd("xtopdf-converter:latest");
        verify(mockDockerClient).startContainerCmd("container-123");
        verify(mockDockerClient).stopContainerCmd("container-123");
        verify(mockDockerClient).removeContainerCmd("container-123");
        verify(mockRestTemplate).postForEntity(contains("/api/convert"), any(), eq(String.class));
    }

    /**
     * Test: Container execution when Docker is unavailable
     * Validates: Requirements 1.3
     */
    @Test
    void testExecuteInContainer_DockerUnavailable() throws Exception {
        // Given: Adapter with Docker client that throws exception
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        // Mock Docker client to throw exception (Docker unavailable)
        when(mockDockerClient.createContainerCmd(anyString()))
                .thenThrow(new DockerException("Docker daemon not running", 500));

        // When/Then: Should throw FileConversionException with descriptive message
        FileConversionException exception = assertThrows(
                FileConversionException.class,
                () -> adapter.executeInContainer(inputFile, outputFile, () -> {})
        );

        assertTrue(exception.getMessage().contains("Failed to execute conversion in Docker container"));
        assertNotNull(exception.getCause());
    }

    /**
     * Test: Container cleanup occurs even on failure
     * Validates: Requirements 1.4
     */
    @Test
    void testCleanup_OccursOnFailure() throws Exception {
        // Given: Adapter that will fail during conversion but succeed in cleanup
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        // Mock Docker commands
        CreateContainerCmd createCmd = mockCreateContainerCmd("container-456");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-456")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-456")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-456")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);

        // Mock container readiness but fail conversion
        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RestClientException("Conversion failed"));

        // When: Execute conversion (will fail)
        assertThrows(FileConversionException.class,
                () -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        // Then: Verify cleanup still occurred
        verify(mockDockerClient).stopContainerCmd("container-456");
        verify(mockDockerClient).removeContainerCmd("container-456");
    }

    /**
     * Test: Cleanup failure is logged but doesn't throw
     * Validates: Requirements 1.4
     */
    @Test
    void testCleanup_FailureIsLogged() throws Exception {
        // Given: Adapter where cleanup will fail
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        // Mock Docker commands
        CreateContainerCmd createCmd = mockCreateContainerCmd("container-789");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-789")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-789")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        
        // Cleanup will fail
        when(mockDockerClient.removeContainerCmd("container-789")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);
        doThrow(new DockerException("Failed to remove container", 500))
                .when(removeCmd).exec();

        // Mock successful conversion
        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // When: Execute conversion
        // Then: Should not throw despite cleanup failure
        assertDoesNotThrow(() -> adapter.executeInContainer(inputFile, outputFile, () -> {}));
        
        // Verify cleanup was attempted
        verify(mockDockerClient).removeContainerCmd("container-789");
    }

    /**
     * Test: Disabled orchestration falls back to local execution
     * Validates: Requirements 1.1
     */
    @Test
    void testExecuteInContainer_DisabledOrchestration() throws Exception {
        // Given: Adapter with orchestration disabled
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        AtomicBoolean converterExecuted = new AtomicBoolean(false);
        Runnable converterLogic = () -> converterExecuted.set(true);

        // When: Execute conversion
        adapter.executeInContainer(inputFile, outputFile, converterLogic);

        // Then: Converter logic should be executed locally
        assertTrue(converterExecuted.get());
        assertFalse(adapter.isEnabled());
    }

    /**
     * Test: Container readiness timeout
     * Validates: Requirements 1.3
     */
    @Test
    void testExecuteInContainer_ContainerNotReady() throws Exception {
        // Given: Adapter where container never becomes ready
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        // Mock Docker commands
        CreateContainerCmd createCmd = mockCreateContainerCmd("container-999");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-999")).thenReturn(startCmd);
        when(mockDockerClient.stopContainerCmd("container-999")).thenReturn(stopCmd);
        when(stopCmd.withTimeout(anyInt())).thenReturn(stopCmd);
        when(mockDockerClient.removeContainerCmd("container-999")).thenReturn(removeCmd);
        when(removeCmd.withForce(anyBoolean())).thenReturn(removeCmd);

        // Container never becomes ready
        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // When/Then: Should timeout and throw exception
        FileConversionException exception = assertThrows(
                FileConversionException.class,
                () -> adapter.executeInContainer(inputFile, outputFile, () -> {})
        );

        assertTrue(exception.getMessage().contains("failed to become ready"));
        
        // Verify cleanup still occurred
        verify(mockDockerClient).stopContainerCmd("container-999");
        verify(mockDockerClient).removeContainerCmd("container-999");
    }

    /**
     * Test: getRuntimeInfo returns Docker information
     * Validates: Requirements 1.1
     */
    @Test
    void testGetRuntimeInfo_Success() throws Exception {
        // Given: Adapter with working Docker client
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        InfoCmd infoCmd = mock(InfoCmd.class);
        Info info = mock(Info.class);
        
        when(mockDockerClient.infoCmd()).thenReturn(infoCmd);
        when(infoCmd.exec()).thenReturn(info);
        when(info.getServerVersion()).thenReturn("20.10.17");
        when(info.getContainers()).thenReturn(5);
        when(info.getImages()).thenReturn(10);

        // When: Get runtime info
        String runtimeInfo = adapter.getRuntimeInfo();

        // Then: Should contain Docker information
        assertNotNull(runtimeInfo);
        assertTrue(runtimeInfo.contains("20.10.17"));
        assertTrue(runtimeInfo.contains("5"));
        assertTrue(runtimeInfo.contains("10"));
    }

    /**
     * Test: getRuntimeInfo handles Docker unavailable
     * Validates: Requirements 1.3
     */
    @Test
    void testGetRuntimeInfo_DockerUnavailable() throws Exception {
        // Given: Adapter where Docker info fails
        DockerContainerAdapter adapter = createAdapterWithMocks(true);
        
        InfoCmd infoCmd = mock(InfoCmd.class);
        when(mockDockerClient.infoCmd()).thenReturn(infoCmd);
        when(infoCmd.exec()).thenThrow(new DockerException("Cannot connect to Docker", 500));

        // When: Get runtime info
        String runtimeInfo = adapter.getRuntimeInfo();

        // Then: Should return error message
        assertNotNull(runtimeInfo);
        assertTrue(runtimeInfo.contains("Failed to get Docker info"));
    }

    /**
     * Test: getRuntimeInfo when orchestration disabled
     * Validates: Requirements 1.1
     */
    @Test
    void testGetRuntimeInfo_OrchestrationDisabled() {
        // Given: Adapter with orchestration disabled
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);

        // When: Get runtime info
        String runtimeInfo = adapter.getRuntimeInfo();

        // Then: Should indicate orchestration is disabled
        assertNotNull(runtimeInfo);
        assertTrue(runtimeInfo.contains("not initialized"));
        assertTrue(runtimeInfo.contains("disabled"));
    }

    /**
     * Test: Memory limit parsing
     * Validates: Requirements 1.1
     */
    @Test
    void testMemoryLimitParsing() throws Exception {
        // Test various memory limit formats
        ContainerConfig configKB = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512k")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig configMB = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig configGB = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("2g")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        // All should create successfully
        assertDoesNotThrow(() -> new DockerContainerAdapter(configKB, false));
        assertDoesNotThrow(() -> new DockerContainerAdapter(configMB, false));
        assertDoesNotThrow(() -> new DockerContainerAdapter(configGB, false));
    }

    /**
     * Test: Cleanup disabled configuration
     * Validates: Requirements 1.4
     */
    @Test
    void testCleanup_DisabledConfiguration() throws Exception {
        // Given: Config with cleanup disabled
        ContainerConfig noCleanupConfig = ContainerConfig.builder()
                .imageName("xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(false)  // Cleanup disabled
                .containerPort(8080)
                .build();

        DockerContainerAdapter adapter = createAdapterWithMocks(noCleanupConfig, true);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        // Mock Docker commands
        CreateContainerCmd createCmd = mockCreateContainerCmd("container-nocleanup");
        StartContainerCmd startCmd = mock(StartContainerCmd.class);

        when(mockDockerClient.createContainerCmd(anyString())).thenReturn(createCmd);
        when(mockDockerClient.startContainerCmd("container-nocleanup")).thenReturn(startCmd);

        // Mock successful conversion
        when(mockRestTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Success", HttpStatus.OK));

        // When: Execute conversion
        assertDoesNotThrow(() -> adapter.executeInContainer(inputFile, outputFile, () -> {}));

        // Then: Cleanup should NOT be called
        verify(mockDockerClient, never()).stopContainerCmd(anyString());
        verify(mockDockerClient, never()).removeContainerCmd(anyString());
    }

    // Helper methods

    private DockerContainerAdapter createAdapterWithMocks(boolean enabled) throws Exception {
        return createAdapterWithMocks(config, enabled);
    }

    private DockerContainerAdapter createAdapterWithMocks(ContainerConfig config, boolean enabled) throws Exception {
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, enabled);
        
        if (enabled) {
            // Use reflection to inject mocked dependencies
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
        when(createCmd.withExposedPorts(any(com.github.dockerjava.api.model.ExposedPort.class))).thenReturn(createCmd);
        when(createCmd.exec()).thenReturn(response);
        when(response.getId()).thenReturn(containerId);
        
        return createCmd;
    }
}
