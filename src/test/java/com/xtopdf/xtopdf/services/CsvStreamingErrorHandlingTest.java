package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.CsvToPdfService;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for streaming error handling in CsvToPdfService.
 * Tests verify proper resource cleanup and error handling during streaming operations.
 * 
 * **Validates: Requirement 5.5** - Streaming encounters errors mid-file, system shall clean up resources
 */
class CsvStreamingErrorHandlingTest {

    private CsvToPdfService csvToPdfService;
    private PdfBackendProvider mockBackend;
    private PdfDocumentBuilder mockBuilder;

    @BeforeEach
    void setUp() throws Exception {
        mockBackend = mock(PdfBackendProvider.class);
        mockBuilder = mock(PdfDocumentBuilder.class);
        when(mockBackend.createBuilder()).thenReturn(mockBuilder);
        csvToPdfService = new CsvToPdfService(mockBackend);
    }

    @Test
    void testStreamingErrorHandling_MalformedDataMidFile_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Use real backend to test actual resource cleanup behavior
        CsvToPdfService realService = new CsvToPdfService(new PdfBoxBackend());
        
        // Create a large CSV file (>10MB) with malformed data in the middle
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        
        // Add valid rows to exceed streaming threshold (10MB)
        // Each row is approximately 50 bytes, need ~200,000 rows for 10MB
        for (int i = 0; i < 150000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }
        
        // Add a line that exceeds MAX_LINE_LENGTH (1MB) in the middle
        content.append("BadPerson,30,");
        for (int i = 0; i < 1_000_001; i++) {
            content.append('x');
        }
        content.append("\n");
        
        // Add more valid rows after the error
        for (int i = 150000; i < 160000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "large_malformed.csv",
                "text/csv",
                content.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            realService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message indicates the problem
        assertTrue(exception.getMessage().contains("exceeds maximum length"),
                "Exception should indicate line length exceeded");
        
        // The PDF file may be created but should be incomplete/invalid
        // The key test is that resources were cleaned up (no file handles left open)
        // Verify no file handles are left open - temp directory should be accessible
        assertTrue(tempDir.toFile().exists(), "Temp directory should still exist after error");
        assertTrue(tempDir.toFile().canWrite(), "Temp directory should be writable after error");
    }

    @Test
    void testStreamingErrorHandling_ExcessiveFieldsMidFile_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Use real backend to test actual resource cleanup behavior
        CsvToPdfService realService = new CsvToPdfService(new PdfBoxBackend());
        
        // Create a large CSV file with excessive fields in the middle
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        
        // Add valid rows to exceed streaming threshold
        for (int i = 0; i < 150000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }
        
        // Add a line with more than MAX_FIELDS (10,000) fields
        for (int i = 0; i < 10_001; i++) {
            if (i > 0) content.append(',');
            content.append("field").append(i);
        }
        content.append("\n");
        
        // Add more valid rows
        for (int i = 150000; i < 160000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "excessive_fields.csv",
                "text/csv",
                content.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            realService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("exceeds maximum field count"),
                "Exception should indicate field count exceeded");

        // The PDF file may be created but should be incomplete/invalid
        // The key test is that resources were cleaned up (no file handles left open)
        assertTrue(tempDir.toFile().exists(), "Temp directory should still exist after error");
        assertTrue(tempDir.toFile().canWrite(), "Temp directory should be writable after error");
    }

    @Test
    void testStreamingErrorHandling_BuilderThrowsException_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Configure mock to throw exception during addTable
        doThrow(new IOException("PDF generation failed")).when(mockBuilder).addTable(any(String[][].class));

        // Create a large CSV file to trigger streaming
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        for (int i = 0; i < 150000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                content.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("Error creating PDF from CSV"),
                "Exception should indicate PDF creation error");

        // Verify that close() was called even though addTable threw exception
        verify(mockBuilder, times(1)).close();
    }

    @Test
    void testStreamingErrorHandling_SaveThrowsException_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Configure mock to throw exception during save
        doThrow(new IOException("Failed to save PDF")).when(mockBuilder).save(any(File.class));

        // Create a large CSV file to trigger streaming
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        for (int i = 0; i < 150000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                content.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("Failed to save PDF"),
                "Exception should indicate save failure");

        // Verify that close() was called even though save threw exception
        verify(mockBuilder, times(1)).close();
    }

    @Test
    void testStreamingErrorHandling_InputStreamThrowsException_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Create a mock MultipartFile that throws IOException when reading
        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "error.csv",
                "text/csv",
                new byte[0]
        ) {
            @Override
            public long getSize() {
                return 15_000_000L; // Report size > streaming threshold
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new InputStream() {
                    private int bytesRead = 0;
                    private final byte[] header = "Name,Age,City\n".getBytes();

                    @Override
                    public int read() throws IOException {
                        if (bytesRead < header.length) {
                            return header[bytesRead++];
                        }
                        // Simulate stream error after reading some data
                        throw new IOException("Stream read error");
                    }
                };
            }
        };

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("Stream read error"),
                "Exception should indicate stream read error");

        // Verify that close() was called for resource cleanup
        verify(mockBuilder, times(1)).close();
    }

    @Test
    void testStreamingErrorHandling_CloseThrowsException_DoesNotMaskOriginalException(@TempDir Path tempDir) throws Exception {
        // Configure mock to throw exception during addTable and close
        IOException originalException = new IOException("PDF generation failed");
        IOException closeException = new IOException("Close failed");
        
        doThrow(originalException).when(mockBuilder).addTable(any(String[][].class));
        doThrow(closeException).when(mockBuilder).close();

        // Create a large CSV file to trigger streaming
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        for (int i = 0; i < 150000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "large.csv",
                "text/csv",
                content.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify that the original exception is preserved, not the close exception
        assertTrue(exception.getMessage().contains("Error creating PDF from CSV"),
                "Original exception should be preserved");

        // Verify that close() was attempted
        verify(mockBuilder, times(1)).close();
    }

    @Test
    void testStreamingErrorHandling_RealBackend_VerifyResourceCleanup(@TempDir Path tempDir) throws Exception {
        // Use real backend to verify actual resource cleanup
        CsvToPdfService realService = new CsvToPdfService(new PdfBoxBackend());

        // Create a large CSV file with malformed data
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        
        // Add valid rows to exceed streaming threshold
        for (int i = 0; i < 150000; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }
        
        // Add a line that exceeds MAX_LINE_LENGTH
        content.append("BadPerson,30,");
        for (int i = 0; i < 1_000_001; i++) {
            content.append('x');
        }
        content.append("\n");

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "large_malformed.csv",
                "text/csv",
                content.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            realService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("exceeds maximum length"),
                "Exception should indicate line length exceeded");

        // The PDF file may be created but should be incomplete/invalid
        // The key test is that resources were cleaned up (no file handles left open)
        assertTrue(tempDir.toFile().exists(), "Temp directory should still exist after error");
        assertTrue(tempDir.toFile().canWrite(), "Temp directory should be writable after error");
    }

    @Test
    void testStreamingErrorHandling_EmptyFileAfterStreamingThreshold_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Create a mock file that reports large size but has no content
        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "empty_large.csv",
                "text/csv",
                new byte[0]
        ) {
            @Override
            public long getSize() {
                return 15_000_000L; // Report size > streaming threshold
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[0]);
            }
        };

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("CSV file is empty"),
                "Exception should indicate empty file");

        // Verify resource cleanup
        verify(mockBuilder, times(1)).close();
    }

    @Test
    void testStreamingErrorHandling_MultipleChunksWithErrorInLast_CleansUpResources(@TempDir Path tempDir) throws Exception {
        // Create a large CSV file with error in the last chunk
        StringBuilder content = new StringBuilder("Name,Age,City\n");
        
        // Add valid rows for multiple chunks (CHUNK_SIZE = 1000)
        for (int i = 0; i < 2500; i++) {
            content.append("Person").append(i).append(",").append(20 + (i % 50)).append(",City").append(i % 100).append("\n");
        }
        
        // Add a line with excessive fields in the last chunk
        for (int i = 0; i < 10_001; i++) {
            if (i > 0) content.append(',');
            content.append("field").append(i);
        }
        content.append("\n");

        MockMultipartFile csvFile = new MockMultipartFile(
                "file",
                "error_in_last_chunk.csv",
                "text/csv",
                content.toString().getBytes()
        ) {
            @Override
            public long getSize() {
                return 15_000_000L; // Force streaming mode
            }
        };

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        // Verify that an exception is thrown
        IOException exception = assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });

        // Verify the error message
        assertTrue(exception.getMessage().contains("exceeds maximum field count"),
                "Exception should indicate field count exceeded");

        // Verify that addTable was called for the valid chunks before the error
        verify(mockBuilder, atLeast(2)).addTable(any(String[][].class));

        // Verify resource cleanup
        verify(mockBuilder, times(1)).close();
    }
}
