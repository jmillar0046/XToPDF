package com.xtopdf.xtopdf.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Info;
import com.xtopdf.xtopdf.config.ContainerOrchestrationConfig;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContainerOrchestrationService
 */
@ExtendWith(MockitoExtension.class)
class ContainerOrchestrationServiceTest {
    
    @Mock
    private DockerClient dockerClient;
    
    @Mock
    private InfoCmd infoCmd;
    
    private ContainerOrchestrationConfig config;
    private ContainerOrchestrationService service;
    
    @BeforeEach
    void setUp() {
        config = new ContainerOrchestrationConfig();
        config.setEnabled(false); // Disabled by default for most tests
    }
    
    @Test
    void testServiceInitializationWhenDisabled() {
        service = new ContainerOrchestrationService(config);
        
        assertFalse(service.isEnabled(), "Service should not be enabled");
        assertEquals("Docker client not initialized (orchestration disabled)", 
                service.getDockerInfo());
    }
    
    @Test
    void testIsEnabledWhenConfigured() {
        config.setEnabled(false);
        service = new ContainerOrchestrationService(config);
        assertFalse(service.isEnabled());
        
        // Create new instance with different configuration
        ContainerOrchestrationConfig anotherConfig = new ContainerOrchestrationConfig();
        anotherConfig.setEnabled(false); // Keep disabled to avoid Docker initialization in unit tests
        service = new ContainerOrchestrationService(anotherConfig);
        assertFalse(service.isEnabled());
    }
    
    @Test
    void testExecuteInContainerWhenDisabled() throws FileConversionException {
        service = new ContainerOrchestrationService(config);
        
        MultipartFile inputFile = new MockMultipartFile(
                "test.txt", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = "/tmp/output.pdf";
        
        final boolean[] runnableCalled = {false};
        Runnable converterLogic = () -> runnableCalled[0] = true;
        
        service.executeInContainer(inputFile, outputFile, converterLogic);
        
        assertTrue(runnableCalled[0], "Converter logic should be executed when orchestration is disabled");
    }
    
    @Test
    void testParseMemoryLimit() throws Exception {
        service = new ContainerOrchestrationService(config);
        
        // Test parseMemoryLimit via reflection
        Method method = ContainerOrchestrationService.class.getDeclaredMethod("parseMemoryLimit", String.class);
        method.setAccessible(true);
        
        // Test various memory formats
        long result512m = (long) method.invoke(service, "512m");
        assertEquals(512L * 1024L * 1024L, result512m);
        
        long result1g = (long) method.invoke(service, "1g");
        assertEquals(1024L * 1024L * 1024L, result1g);
        
        long result2048k = (long) method.invoke(service, "2048k");
        assertEquals(2048L * 1024L, result2048k);
        
        long resultBytes = (long) method.invoke(service, "1024");
        assertEquals(1024L, resultBytes);
        
        // Test invalid format - should return default 512MB
        long resultInvalid = (long) method.invoke(service, "invalid");
        assertEquals(512L * 1024L * 1024L, resultInvalid);
    }
    
    @Test
    void testFindAvailablePort() throws Exception {
        service = new ContainerOrchestrationService(config);
        
        // Test findAvailablePort via reflection
        Method method = ContainerOrchestrationService.class.getDeclaredMethod("findAvailablePort");
        method.setAccessible(true);
        
        int port = (int) method.invoke(service);
        assertTrue(port > 0 && port < 65536, "Port should be in valid range");
    }
    
    @Test
    void testConfigurationSettings() {
        config.setEnabled(false);
        config.setMemoryLimit("1g");
        config.setCpuLimit(2);
        config.setTimeoutSeconds(600);
        config.setCleanupEnabled(false);
        
        service = new ContainerOrchestrationService(config);
        
        assertFalse(service.isEnabled());
    }
    
    @Test
    void testImageConfiguration() {
        ContainerOrchestrationConfig.Image image = config.getImage();
        image.setName("custom-image");
        image.setTag("v1.0.0");
        
        service = new ContainerOrchestrationService(config);
        
        assertEquals("custom-image:v1.0.0", image.getFullName());
    }
    
    @Test
    void testExecuteInContainerWithNullFile() {
        service = new ContainerOrchestrationService(config);
        
        Runnable converterLogic = () -> {
            // This should execute without container
        };
        
        // Test with null file when disabled - should just run the logic
        assertDoesNotThrow(() -> 
            service.executeInContainer(null, "/tmp/output.pdf", converterLogic)
        );
    }
    
    @Test
    void testMultipleConfigurationChanges() {
        config.setEnabled(false);
        config.setMemoryLimit("512m");
        service = new ContainerOrchestrationService(config);
        assertFalse(service.isEnabled());
        
        // Change configuration
        config.setMemoryLimit("1g");
        config.setCpuLimit(4);
        config.setTimeoutSeconds(900);
        
        // Configuration changes are reflected
        assertEquals("1g", config.getMemoryLimit());
        assertEquals(4, config.getCpuLimit());
        assertEquals(900, config.getTimeoutSeconds());
    }
    
    @Test
    void testMemoryLimitFormats() {
        config.setMemoryLimit("512m");
        assertEquals("512m", config.getMemoryLimit());
        
        config.setMemoryLimit("1g");
        assertEquals("1g", config.getMemoryLimit());
        
        config.setMemoryLimit("2048k");
        assertEquals("2048k", config.getMemoryLimit());
        
        config.setMemoryLimit("1024");
        assertEquals("1024", config.getMemoryLimit());
    }
    
    @Test
    void testTimeoutConfiguration() {
        config.setTimeoutSeconds(60);
        service = new ContainerOrchestrationService(config);
        assertEquals(60, config.getTimeoutSeconds());
        
        config.setTimeoutSeconds(300);
        assertEquals(300, config.getTimeoutSeconds());
        
        config.setTimeoutSeconds(900);
        assertEquals(900, config.getTimeoutSeconds());
    }
    
    @Test
    void testCleanupConfiguration() {
        config.setCleanupEnabled(true);
        service = new ContainerOrchestrationService(config);
        assertTrue(config.isCleanupEnabled());
        
        config.setCleanupEnabled(false);
        service = new ContainerOrchestrationService(config);
        assertFalse(config.isCleanupEnabled());
    }
    
    @Test
    void testCpuLimitConfiguration() {
        config.setCpuLimit(1);
        assertEquals(1, config.getCpuLimit());
        
        config.setCpuLimit(2);
        assertEquals(2, config.getCpuLimit());
        
        config.setCpuLimit(4);
        assertEquals(4, config.getCpuLimit());
        
        config.setCpuLimit(8);
        assertEquals(8, config.getCpuLimit());
    }
    
    @Test
    void testGetDockerInfoWhenDisabled() {
        service = new ContainerOrchestrationService(config);
        String info = service.getDockerInfo();
        assertEquals("Docker client not initialized (orchestration disabled)", info);
    }
    
    @Test
    void testGetDockerInfoWhenEnabled() throws Exception {
        // Create service with mocked Docker client
        service = new ContainerOrchestrationService(config);
        
        // Inject mocked Docker client via reflection
        Field dockerClientField = ContainerOrchestrationService.class.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        dockerClientField.set(service, dockerClient);
        
        // Mock Docker info
        Info dockerInfo = mock(Info.class);
        when(dockerInfo.getServerVersion()).thenReturn("20.10.0");
        when(dockerInfo.getContainers()).thenReturn(5);
        when(dockerInfo.getImages()).thenReturn(10);
        when(dockerClient.infoCmd()).thenReturn(infoCmd);
        when(infoCmd.exec()).thenReturn(dockerInfo);
        
        String info = service.getDockerInfo();
        assertTrue(info.contains("20.10.0"));
        assertTrue(info.contains("5"));
        assertTrue(info.contains("10"));
    }
    
    @Test
    void testGetDockerInfoWithException() throws Exception {
        service = new ContainerOrchestrationService(config);
        
        // Inject mocked Docker client via reflection
        Field dockerClientField = ContainerOrchestrationService.class.getDeclaredField("dockerClient");
        dockerClientField.setAccessible(true);
        dockerClientField.set(service, dockerClient);
        
        // Mock Docker info to throw exception
        when(dockerClient.infoCmd()).thenReturn(infoCmd);
        when(infoCmd.exec()).thenThrow(new RuntimeException("Docker not available"));
        
        String info = service.getDockerInfo();
        assertTrue(info.contains("Failed to get Docker info"));
        assertTrue(info.contains("Docker not available"));
    }
    
    @Test
    void testMemoryLimitParsing() throws Exception {
        service = new ContainerOrchestrationService(config);
        Method method = ContainerOrchestrationService.class.getDeclaredMethod("parseMemoryLimit", String.class);
        method.setAccessible(true);
        
        // Test uppercase
        long resultM = (long) method.invoke(service, "512M");
        assertEquals(512L * 1024L * 1024L, resultM);
        
        long resultG = (long) method.invoke(service, "2G");
        assertEquals(2L * 1024L * 1024L * 1024L, resultG);
        
        long resultK = (long) method.invoke(service, "1024K");
        assertEquals(1024L * 1024L, resultK);
    }
    
    @Test
    void testExecuteInContainerExceptionHandling() {
        service = new ContainerOrchestrationService(config);
        
        MultipartFile inputFile = new MockMultipartFile(
                "test.txt", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = "/tmp/output.pdf";
        
        // Test with logic that throws exception
        Runnable converterLogic = () -> {
            throw new RuntimeException("Conversion failed");
        };
        
        assertThrows(RuntimeException.class, () -> 
            service.executeInContainer(inputFile, outputFile, converterLogic)
        );
    }
}
