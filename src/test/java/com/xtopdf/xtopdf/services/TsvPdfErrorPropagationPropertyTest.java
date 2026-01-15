package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import net.jqwik.api.*;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for PDF generation error propagation in TSV service.
 * Tests that PDF backend errors are properly wrapped and propagated.
 */
class TsvPdfErrorPropagationPropertyTest {

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 5: PDF Generation Error Propagation")
    void pdfGenerationErrorPropagation(@ForAll String errorMessage) throws Exception {
        // Mock PDF backend to throw exception
        PdfBackendProvider mockBackend = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        
        when(mockBackend.createBuilder()).thenReturn(mockBuilder);
        doThrow(new RuntimeException(errorMessage)).when(mockBuilder).addTable(any());
        
        TsvToPdfService service = new TsvToPdfService(mockBackend);
        
        // Create valid TSV content
        String content = "Name\tAge\nJohn\t30";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file",
                "test.tsv",
                "text/tab-separated-values",
                content.getBytes()
        );
        
        File pdfFile = new File("output.pdf");
        
        // Verify IOException is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            service.convertTsvToPdf(tsvFile, pdfFile);
        });
        
        // Verify exception message indicates PDF creation failure
        assertTrue(exception.getMessage().contains("Error creating PDF from TSV"),
                "Exception message should indicate PDF creation failure");
        assertTrue(exception.getMessage().contains(errorMessage),
                "Exception message should contain original error message");
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 5: PDF Generation Error Propagation")
    void pdfBuilderCloseErrorPropagation(@ForAll String errorMessage) throws Exception {
        // Mock PDF backend to throw exception on close
        PdfBackendProvider mockBackend = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        
        when(mockBackend.createBuilder()).thenReturn(mockBuilder);
        doNothing().when(mockBuilder).addTable(any());
        doThrow(new RuntimeException(errorMessage)).when(mockBuilder).close();
        
        TsvToPdfService service = new TsvToPdfService(mockBackend);
        
        // Create valid TSV content
        String content = "Name\tAge\nJohn\t30";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file",
                "test.tsv",
                "text/tab-separated-values",
                content.getBytes()
        );
        
        File pdfFile = new File("output.pdf");
        
        // Verify IOException is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            service.convertTsvToPdf(tsvFile, pdfFile);
        });
        
        // Verify exception message indicates PDF creation failure
        assertTrue(exception.getMessage().contains("Error creating PDF from TSV"),
                "Exception message should indicate PDF creation failure");
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 5: PDF Generation Error Propagation")
    void pdfSaveErrorPropagation(@ForAll String errorMessage) throws Exception {
        // Mock PDF backend to throw exception on save
        PdfBackendProvider mockBackend = mock(PdfBackendProvider.class);
        PdfDocumentBuilder mockBuilder = mock(PdfDocumentBuilder.class);
        
        when(mockBackend.createBuilder()).thenReturn(mockBuilder);
        doNothing().when(mockBuilder).addTable(any());
        doThrow(new IOException(errorMessage)).when(mockBuilder).save(any(File.class));
        
        TsvToPdfService service = new TsvToPdfService(mockBackend);
        
        // Create valid TSV content
        String content = "Name\tAge\nJohn\t30";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file",
                "test.tsv",
                "text/tab-separated-values",
                content.getBytes()
        );
        
        File pdfFile = new File("output.pdf");
        
        // Verify IOException is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            service.convertTsvToPdf(tsvFile, pdfFile);
        });
        
        // Verify exception message indicates PDF creation failure
        assertTrue(exception.getMessage().contains("Error creating PDF from TSV"),
                "Exception message should indicate PDF creation failure");
    }
}
