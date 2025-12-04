package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.ports.ContainerRuntimePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContainerOrchestrationService with hexagonal architecture
 */
@ExtendWith(MockitoExtension.class)
class ContainerOrchestrationServiceTest {
    
    @Mock
    private ContainerRuntimePort containerRuntime;
    
    private ContainerOrchestrationService service;
    
    @BeforeEach
    void setUp() {
        service = new ContainerOrchestrationService(containerRuntime);
    }
    
    @Test
    void testServiceInitializationWhenDisabled() {
        when(containerRuntime.isEnabled()).thenReturn(false);
        when(containerRuntime.getRuntimeInfo()).thenReturn("Runtime disabled");
        
        assertFalse(service.isEnabled(), "Service should not be enabled");
        assertEquals("Runtime disabled", service.getDockerInfo());
    }
    
    @Test
    void testIsEnabledWhenConfigured() {
        when(containerRuntime.isEnabled()).thenReturn(false);
        assertFalse(service.isEnabled());
        
        when(containerRuntime.isEnabled()).thenReturn(true);
        assertTrue(service.isEnabled());
    }
    
    @Test
    void testExecuteInContainerWhenDisabled() throws FileConversionException {
        MultipartFile inputFile = new MockMultipartFile(
                "test.txt", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = "/tmp/output.pdf";
        
        final boolean[] runnableCalled = {false};
        Runnable converterLogic = () -> runnableCalled[0] = true;
        
        // Mock the behavior: when disabled, it should call converterLogic
        doAnswer(invocation -> {
            Runnable logic = invocation.getArgument(2);
            logic.run();
            return null;
        }).when(containerRuntime).executeInContainer(any(), any(), any());
        
        service.executeInContainer(inputFile, outputFile, converterLogic);
        
        verify(containerRuntime, times(1)).executeInContainer(inputFile, outputFile, converterLogic);
    }
    
    @Test
    void testExecuteInContainerWithException() throws FileConversionException {
        MultipartFile inputFile = new MockMultipartFile(
                "test.txt", "test.txt", "text/plain", "test content".getBytes());
        String outputFile = "/tmp/output.pdf";
        
        Runnable converterLogic = () -> {};
        
        // Mock exception being thrown
        doThrow(new FileConversionException("Container failed"))
                .when(containerRuntime).executeInContainer(any(), any(), any());
        
        assertThrows(FileConversionException.class, () -> 
            service.executeInContainer(inputFile, outputFile, converterLogic)
        );
    }
    
    @Test
    void testGetDockerInfoWithDifferentRuntimes() {
        when(containerRuntime.getRuntimeInfo()).thenReturn("Docker version: 20.10.0");
        assertEquals("Docker version: 20.10.0", service.getDockerInfo());
        
        when(containerRuntime.getRuntimeInfo()).thenReturn("Podman version: 4.0.0");
        assertEquals("Podman version: 4.0.0", service.getDockerInfo());
    }
    
    @Test
    void testExecuteInContainerWithNullFile() throws FileConversionException {
        Runnable converterLogic = () -> {};
        
        doNothing().when(containerRuntime).executeInContainer(any(), any(), any());
        
        assertDoesNotThrow(() -> 
            service.executeInContainer(null, "/tmp/output.pdf", converterLogic)
        );
        
        verify(containerRuntime, times(1)).executeInContainer(null, "/tmp/output.pdf", converterLogic);
    }
    
    @Test
    void testMultipleConversions() throws FileConversionException {
        MultipartFile inputFile1 = new MockMultipartFile(
                "test1.txt", "test1.txt", "text/plain", "content 1".getBytes());
        MultipartFile inputFile2 = new MockMultipartFile(
                "test2.txt", "test2.txt", "text/plain", "content 2".getBytes());
        
        Runnable converterLogic = () -> {};
        
        doNothing().when(containerRuntime).executeInContainer(any(), any(), any());
        
        service.executeInContainer(inputFile1, "/tmp/output1.pdf", converterLogic);
        service.executeInContainer(inputFile2, "/tmp/output2.pdf", converterLogic);
        
        verify(containerRuntime, times(2)).executeInContainer(any(), any(), any());
    }
}
