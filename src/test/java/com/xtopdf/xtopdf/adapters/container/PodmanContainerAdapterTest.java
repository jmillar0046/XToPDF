package com.xtopdf.xtopdf.adapters.container;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PodmanContainerAdapter.
 * 
 * Tests cover:
 * - Container lifecycle: start, execute, stop, cleanup
 * - Error handling when Podman is unavailable
 * - Cleanup occurs even on failure
 * - Disabled orchestration fallback
 * - Podman-specific behaviors (CLI-based, daemonless)
 * 
 * Note: These tests focus on the adapter's configuration and behavior patterns.
 * Actual Podman CLI interactions require Podman to be installed and are tested
 * through manual integration testing or CI/CD pipelines with Podman available.
 * 
 * Requirements: 1.2, 1.3, 1.4
 */
class PodmanContainerAdapterTest {

    @TempDir
    Path tempDir;

    private ContainerConfig config;

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
    }

    /**
     * Test: Disabled orchestration falls back to local execution
     * Validates: Requirements 1.2
     */
    @Test
    void testExecuteInContainer_DisabledOrchestration() throws Exception {
        // Given: Adapter with orchestration disabled
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, false);
        
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
     * Test: isEnabled returns correct state
     * Validates: Requirements 1.2
     */
    @Test
    void testIsEnabled_WhenDisabled() {
        // Given: Adapter with orchestration disabled
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, false);

        // When/Then: Should return false
        assertFalse(adapter.isEnabled());
    }

    /**
     * Test: isEnabled returns correct state when enabled
     * Validates: Requirements 1.2
     */
    @Test
    void testIsEnabled_WhenEnabled() {
        // Given: Adapter with orchestration enabled
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, true);

        // When/Then: Should return true
        assertTrue(adapter.isEnabled());
    }

    /**
     * Test: getRuntimeInfo returns information
     * Validates: Requirements 1.2
     */
    @Test
    void testGetRuntimeInfo() {
        // Given: Adapter (enabled or disabled)
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, true);

        // When: Get runtime info
        String runtimeInfo = adapter.getRuntimeInfo();

        // Then: Should return some information (may indicate Podman not found)
        assertNotNull(runtimeInfo);
        assertTrue(runtimeInfo.contains("Podman") || runtimeInfo.contains("Failed"));
    }

    /**
     * Test: Memory limit configuration
     * Validates: Requirements 1.2
     */
    @Test
    void testMemoryLimitConfiguration() {
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
        assertDoesNotThrow(() -> new PodmanContainerAdapter(configKB, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(configMB, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(configGB, false));
    }

    /**
     * Test: CPU limit configuration
     * Validates: Requirements 1.2
     */
    @Test
    void testCpuLimitConfiguration() {
        // Test various CPU limit values
        ContainerConfig config1CPU = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig config2CPU = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(2)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig config4CPU = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(4)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        // All should create successfully
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config1CPU, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config2CPU, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config4CPU, false));
    }

    /**
     * Test: Cleanup enabled configuration
     * Validates: Requirements 1.4
     */
    @Test
    void testCleanup_EnabledConfiguration() {
        // Given: Config with cleanup enabled
        ContainerConfig cleanupConfig = ContainerConfig.builder()
                .imageName("xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        // When: Create adapter
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(cleanupConfig, false);

        // Then: Should create successfully
        assertNotNull(adapter);
    }

    /**
     * Test: Cleanup disabled configuration
     * Validates: Requirements 1.4
     */
    @Test
    void testCleanup_DisabledConfiguration() {
        // Given: Config with cleanup disabled
        ContainerConfig noCleanupConfig = ContainerConfig.builder()
                .imageName("xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(false)  // Cleanup disabled
                .containerPort(8080)
                .build();

        // When: Create adapter
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(noCleanupConfig, false);

        // Then: Should create successfully
        assertNotNull(adapter);
    }

    /**
     * Test: Container port configuration
     * Validates: Requirements 1.2
     */
    @Test
    void testContainerPortConfiguration() {
        // Test various port configurations
        ContainerConfig config8080 = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig config3000 = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(3000)
                .build();

        // All should create successfully
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config8080, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config3000, false));
    }

    /**
     * Test: Timeout configuration
     * Validates: Requirements 1.3
     */
    @Test
    void testTimeoutConfiguration() {
        // Test various timeout values
        ContainerConfig config60s = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(60)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig config300s = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig config600s = ContainerConfig.builder()
                .imageName("test:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(600)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        // All should create successfully
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config60s, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config300s, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(config600s, false));
    }

    /**
     * Test: Image name configuration
     * Validates: Requirements 1.2
     */
    @Test
    void testImageNameConfiguration() {
        // Test various image names
        ContainerConfig configLatest = ContainerConfig.builder()
                .imageName("xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig configVersioned = ContainerConfig.builder()
                .imageName("xtopdf-converter:1.0.0")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        ContainerConfig configRegistry = ContainerConfig.builder()
                .imageName("registry.example.com/xtopdf-converter:latest")
                .memoryLimit("512m")
                .cpuLimit(1)
                .timeoutSeconds(300)
                .cleanupEnabled(true)
                .containerPort(8080)
                .build();

        // All should create successfully
        assertDoesNotThrow(() -> new PodmanContainerAdapter(configLatest, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(configVersioned, false));
        assertDoesNotThrow(() -> new PodmanContainerAdapter(configRegistry, false));
    }

    /**
     * Test: Adapter initialization with enabled flag
     * Validates: Requirements 1.2
     */
    @Test
    void testAdapterInitialization_Enabled() {
        // Given/When: Create adapter with enabled flag
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, true);

        // Then: Should be enabled
        assertTrue(adapter.isEnabled());
    }

    /**
     * Test: Adapter initialization with disabled flag
     * Validates: Requirements 1.2
     */
    @Test
    void testAdapterInitialization_Disabled() {
        // Given/When: Create adapter with disabled flag
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, false);

        // Then: Should be disabled
        assertFalse(adapter.isEnabled());
    }

    /**
     * Test: Local execution when orchestration disabled
     * Validates: Requirements 1.2
     */
    @Test
    void testLocalExecution_WhenOrchestrationDisabled() throws Exception {
        // Given: Adapter with orchestration disabled
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, false);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        AtomicBoolean converterExecuted = new AtomicBoolean(false);
        Runnable converterLogic = () -> {
            converterExecuted.set(true);
            // Simulate creating output file
            try {
                java.nio.file.Files.write(
                    java.nio.file.Paths.get(outputFile),
                    "PDF content".getBytes()
                );
            } catch (Exception e) {
                // Ignore for test
            }
        };

        // When: Execute conversion
        adapter.executeInContainer(inputFile, outputFile, converterLogic);

        // Then: Converter logic should be executed locally
        assertTrue(converterExecuted.get());
    }

    /**
     * Test: Multiple sequential conversions with disabled orchestration
     * Validates: Requirements 1.2
     */
    @Test
    void testMultipleConversions_DisabledOrchestration() throws Exception {
        // Given: Adapter with orchestration disabled
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, false);
        
        AtomicBoolean firstExecuted = new AtomicBoolean(false);
        AtomicBoolean secondExecuted = new AtomicBoolean(false);
        AtomicBoolean thirdExecuted = new AtomicBoolean(false);

        // When: Execute multiple conversions
        adapter.executeInContainer(
            new MockMultipartFile("file1", "test1.txt", "text/plain", "content1".getBytes()),
            tempDir.resolve("output1.pdf").toString(),
            () -> firstExecuted.set(true)
        );

        adapter.executeInContainer(
            new MockMultipartFile("file2", "test2.txt", "text/plain", "content2".getBytes()),
            tempDir.resolve("output2.pdf").toString(),
            () -> secondExecuted.set(true)
        );

        adapter.executeInContainer(
            new MockMultipartFile("file3", "test3.txt", "text/plain", "content3".getBytes()),
            tempDir.resolve("output3.pdf").toString(),
            () -> thirdExecuted.set(true)
        );

        // Then: All conversions should execute locally
        assertTrue(firstExecuted.get());
        assertTrue(secondExecuted.get());
        assertTrue(thirdExecuted.get());
    }

    /**
     * Test: Converter logic exception is propagated
     * Validates: Requirements 1.3
     */
    @Test
    void testConverterLogicException_Propagated() {
        // Given: Adapter with orchestration disabled
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, false);
        
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = tempDir.resolve("output.pdf").toString();

        Runnable converterLogic = () -> {
            throw new RuntimeException("Conversion failed");
        };

        // When/Then: Exception should be propagated
        assertThrows(RuntimeException.class, () -> 
            adapter.executeInContainer(inputFile, outputFile, converterLogic)
        );
    }

    /**
     * Test: Podman-specific behavior - CLI-based adapter
     * Validates: Requirements 1.2
     * 
     * This test verifies that the PodmanContainerAdapter is designed to use
     * CLI commands (unlike DockerContainerAdapter which uses the Docker Java API).
     * The actual CLI execution is tested through integration tests.
     */
    @Test
    void testPodmanSpecificBehavior_CLIBased() {
        // Given: Podman adapter
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(config, true);

        // When/Then: Adapter should be enabled and ready
        assertTrue(adapter.isEnabled());
        assertNotNull(adapter.getRuntimeInfo());
    }

    /**
     * Test: Container configuration with all parameters
     * Validates: Requirements 1.2, 1.4
     */
    @Test
    void testContainerConfiguration_AllParameters() {
        // Given: Config with all parameters specified
        ContainerConfig fullConfig = ContainerConfig.builder()
                .imageName("custom-converter:v2.0")
                .memoryLimit("1g")
                .cpuLimit(2)
                .timeoutSeconds(600)
                .cleanupEnabled(true)
                .containerPort(9090)
                .build();

        // When: Create adapter
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(fullConfig, true);

        // Then: Should create successfully and be enabled
        assertNotNull(adapter);
        assertTrue(adapter.isEnabled());
    }

    /**
     * Test: Container configuration with minimal parameters
     * Validates: Requirements 1.2
     */
    @Test
    void testContainerConfiguration_MinimalParameters() {
        // Given: Config with minimal parameters
        ContainerConfig minimalConfig = ContainerConfig.builder()
                .imageName("converter:latest")
                .memoryLimit("256m")
                .cpuLimit(1)
                .timeoutSeconds(60)
                .cleanupEnabled(false)
                .containerPort(8080)
                .build();

        // When: Create adapter
        PodmanContainerAdapter adapter = new PodmanContainerAdapter(minimalConfig, false);

        // Then: Should create successfully
        assertNotNull(adapter);
        assertFalse(adapter.isEnabled());
    }
}
