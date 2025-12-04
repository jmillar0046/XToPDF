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
        assertEquals("512m", config.getMemoryLimit(), "Default memory limit should be 512m");
        assertEquals(1, config.getCpuLimit(), "Default CPU limit should be 1");
        assertEquals(300, config.getTimeoutSeconds(), "Default timeout should be 300 seconds");
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
        config.setMemoryLimit("1g");
        config.setCpuLimit(2);
        config.setTimeoutSeconds(600);
        config.setCleanupEnabled(false);
        
        assertTrue(config.isEnabled());
        assertEquals("1g", config.getMemoryLimit());
        assertEquals(2, config.getCpuLimit());
        assertEquals(600, config.getTimeoutSeconds());
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
}
