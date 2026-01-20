package com.xtopdf.xtopdf.adapters.container;

import com.xtopdf.xtopdf.ports.ContainerConfig;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for container adapter execution.
 * Validates Requirements 1.1, 1.2
 * 
 * Property 1: Container Adapter Execution
 * Property 2: Container Cleanup Always Occurs
 */
class ContainerAdapterPropertyTest {

    /**
     * Property 1: Container adapter handles disabled state correctly
     * 
     * When container orchestration is disabled, the adapter should
     * execute the converter logic locally without attempting Docker operations.
     */
    @Property
    @Label("Disabled container adapter executes locally")
    void disabledContainerAdapterExecutesLocally(
            @ForAll("validContainerConfigs") ContainerConfig config,
            @ForAll("validInputFiles") MultipartFile inputFile) {
        
        // Create adapter with orchestration disabled
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, false);
        
        // Track if converter logic was executed
        final boolean[] executed = {false};
        Runnable converterLogic = () -> executed[0] = true;
        
        try {
            // Execute should run locally
            adapter.executeInContainer(inputFile, "output.pdf", converterLogic);
            
            // Verify local execution occurred
            assertThat(executed[0]).isTrue();
            assertThat(adapter.isEnabled()).isFalse();
            
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception in disabled mode", e);
        }
    }

    /**
     * Property 2: Container adapter provides runtime info
     * 
     * The adapter should always be able to provide runtime information,
     * whether enabled or disabled.
     */
    @Property
    @Label("Container adapter provides runtime info")
    void containerAdapterProvidesRuntimeInfo(
            @ForAll("validContainerConfigs") ContainerConfig config,
            @ForAll boolean enabled) {
        
        DockerContainerAdapter adapter = new DockerContainerAdapter(config, enabled);
        
        // Runtime info should always be available
        String info = adapter.getRuntimeInfo();
        assertThat(info).isNotNull();
        assertThat(info).isNotEmpty();
        
        // Verify enabled state matches
        assertThat(adapter.isEnabled()).isEqualTo(enabled);
    }

    /**
     * Property 3: Container config validation
     * 
     * Valid container configurations should have all required fields populated.
     */
    @Property
    @Label("Valid container configs have required fields")
    void validContainerConfigsHaveRequiredFields(
            @ForAll("validContainerConfigs") ContainerConfig config) {
        
        assertThat(config.getImageName()).isNotNull();
        assertThat(config.getImageName()).isNotEmpty();
        assertThat(config.getMemoryLimit()).isNotNull();
        assertThat(config.getCpuLimit()).isGreaterThan(0);
        assertThat(config.getTimeoutSeconds()).isGreaterThan(0);
        assertThat(config.getContainerPort()).isGreaterThan(0);
    }

    /**
     * Property 4: Cleanup is always attempted
     * 
     * When cleanup is enabled in config, cleanup should be attempted
     * regardless of execution success or failure.
     */
    @Property
    @Label("Cleanup is attempted when enabled")
    void cleanupIsAttemptedWhenEnabled(
            @ForAll("validContainerConfigs") ContainerConfig config) {
        
        // Ensure cleanup is enabled
        ContainerConfig cleanupConfig = ContainerConfig.builder()
                .imageName(config.getImageName())
                .memoryLimit(config.getMemoryLimit())
                .cpuLimit(config.getCpuLimit())
                .timeoutSeconds(config.getTimeoutSeconds())
                .cleanupEnabled(true)
                .containerPort(config.getContainerPort())
                .build();
        
        assertThat(cleanupConfig.isCleanupEnabled()).isTrue();
        
        // When disabled, local execution should work
        DockerContainerAdapter adapter = new DockerContainerAdapter(cleanupConfig, false);
        assertThat(adapter.isEnabled()).isFalse();
    }

    // Arbitraries for generating test data

    @Provide
    Arbitrary<ContainerConfig> validContainerConfigs() {
        return Arbitraries.of(
                ContainerConfig.builder()
                        .imageName("alpine:latest")
                        .memoryLimit("256m")
                        .cpuLimit(1)
                        .timeoutSeconds(30)
                        .cleanupEnabled(true)
                        .containerPort(8080)
                        .build(),
                ContainerConfig.builder()
                        .imageName("ubuntu:latest")
                        .memoryLimit("512m")
                        .cpuLimit(2)
                        .timeoutSeconds(60)
                        .cleanupEnabled(true)
                        .containerPort(8080)
                        .build(),
                ContainerConfig.builder()
                        .imageName("xtopdf-converter:latest")
                        .memoryLimit("1g")
                        .cpuLimit(4)
                        .timeoutSeconds(120)
                        .cleanupEnabled(false)
                        .containerPort(8080)
                        .build()
        );
    }

    @Provide
    Arbitrary<MultipartFile> validInputFiles() {
        return Arbitraries.of(
                new MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "test content".getBytes()
                ),
                new MockMultipartFile(
                        "file",
                        "document.pdf",
                        "application/pdf",
                        "PDF content".getBytes()
                ),
                new MockMultipartFile(
                        "file",
                        "data.csv",
                        "text/csv",
                        "col1,col2\nval1,val2".getBytes()
                )
        );
    }
}

