package com.xtopdf.xtopdf.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContainerOrchestrationConfig
 */
class ContainerOrchestrationConfigTest {
    
    @Test
    void testDefaultValues() {
        ContainerOrchestrationConfig config = new ContainerOrchestrationConfig();
        
        assertFalse(config.isEnabled(), "Container orchestration should be disabled by default");
        assertEquals("podman", config.getRuntime(), "Default runtime should be podman");
        assertEquals("512m", config.getMemoryLimit(), "Default memory limit should be 512m");
        assertEquals(1, config.getCpuLimit(), "Default CPU limit should be 1");
        assertEquals(300, config.getTimeoutSeconds(), "Default timeout should be 300 seconds");
        assertEquals(8080, config.getContainerPort(), "Default container port should be 8080");
        assertTrue(config.isCleanupEnabled(), "Cleanup should be enabled by default");
    }
    
    @Test
    void testImageConfiguration() {
        ContainerOrchestrationConfig config = new ContainerOrchestrationConfig();
        ContainerOrchestrationConfig.Image image = config.getImage();
        
        assertNotNull(image, "Image configuration should not be null");
        assertEquals("xtopdf-converter", image.getName(), "Default image name should be xtopdf-converter");
        assertEquals("latest", image.getTag(), "Default image tag should be latest");
        assertEquals("xtopdf-converter:latest", image.getFullName(), 
                "Full image name should combine name and tag");
    }
    
    @Test
    void testSetters() {
        ContainerOrchestrationConfig config = new ContainerOrchestrationConfig();
        
        config.setEnabled(true);
        config.setRuntime("podman");
        config.setMemoryLimit("1g");
        config.setCpuLimit(2);
        config.setTimeoutSeconds(600);
        config.setContainerPort(9090);
        config.setCleanupEnabled(false);
        
        assertTrue(config.isEnabled());
        assertEquals("podman", config.getRuntime());
        assertEquals("1g", config.getMemoryLimit());
        assertEquals(2, config.getCpuLimit());
        assertEquals(600, config.getTimeoutSeconds());
        assertEquals(9090, config.getContainerPort());
        assertFalse(config.isCleanupEnabled());
    }
    
    @Test
    void testImageSetters() {
        ContainerOrchestrationConfig config = new ContainerOrchestrationConfig();
        ContainerOrchestrationConfig.Image image = config.getImage();
        
        image.setName("custom-converter");
        image.setTag("v1.0.0");
        
        assertEquals("custom-converter", image.getName());
        assertEquals("v1.0.0", image.getTag());
        assertEquals("custom-converter:v1.0.0", image.getFullName());
    }
    
    @Test
    void testImageFullNameWithDifferentValues() {
        ContainerOrchestrationConfig.Image image = new ContainerOrchestrationConfig.Image();
        
        image.setName("test");
        image.setTag("beta");
        assertEquals("test:beta", image.getFullName());
        
        image.setName("prod-service");
        image.setTag("stable");
        assertEquals("prod-service:stable", image.getFullName());
    }
    
    @Test
    void testRuntimeConfiguration() {
        ContainerOrchestrationConfig config = new ContainerOrchestrationConfig();
        
        config.setRuntime("docker");
        assertEquals("docker", config.getRuntime());
        
        config.setRuntime("podman");
        assertEquals("podman", config.getRuntime());
        
        config.setRuntime("containerd");
        assertEquals("containerd", config.getRuntime());
    }
}
