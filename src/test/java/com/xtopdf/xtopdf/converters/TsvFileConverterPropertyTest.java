package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.TsvToPdfService;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for TSV file converter.
 * Tests IOException wrapping behavior with random error messages.
 */
class TsvFileConverterPropertyTest {

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 3: IOException Wrapping in Converter")
    void ioExceptionWrappingInConverter(@ForAll String errorMessage) throws IOException, FileConversionException {
        // Mock service to throw IOException with random message
        TsvToPdfService mockService = mock(TsvToPdfService.class);
        doThrow(new IOException(errorMessage)).when(mockService).convertTsvToPdf(any(), any());
        
        TsvFileConverter converter = new TsvFileConverter(mockService);
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                "content".getBytes()
        );
        
        // Verify RuntimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            converter.convertToPDF(inputFile, "output.pdf");
        });
        
        // Verify exception message contains original message
        assertTrue(exception.getMessage().contains(errorMessage), 
                "Exception message should contain original error message");
        assertTrue(exception.getMessage().contains("Error converting TSV to PDF"),
                "Exception message should indicate TSV conversion error");
        
        // Verify cause is the original IOException
        assertInstanceOf(IOException.class, exception.getCause(),
                "Cause should be the original IOException");
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 3: IOException Wrapping in Converter")
    void successfulConversionDelegates(@ForAll String filename) throws IOException, FileConversionException {
        // Mock service for successful conversion
        TsvToPdfService mockService = mock(TsvToPdfService.class);
        doNothing().when(mockService).convertTsvToPdf(any(), any());
        
        TsvFileConverter converter = new TsvFileConverter(mockService);
        
        // Use valid filename
        String validFilename = filename.replaceAll("[^a-zA-Z0-9]", "_") + ".tsv";
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", 
                validFilename, 
                "text/tab-separated-values", 
                "content".getBytes()
        );
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            converter.convertToPDF(inputFile, "output.pdf");
        });
        
        // Verify service was called
        verify(mockService, times(1)).convertTsvToPdf(any(), any());
    }
}
