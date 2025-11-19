package com.xtopdf.xtopdf.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfFileHelperTest {

    @Test
    void testProcessPdfFile_Success() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ResponseEntity<byte[]> response = PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    // Simple processor that doesn't modify the file
                    assertTrue(file.exists());
                },
                "output.pdf"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("output.pdf"));
    }

    @Test
    void testProcessPdfFile_ProcessorModifiesFile() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Original content".getBytes()
        );

        ResponseEntity<byte[]> response = PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    // Modify the file
                    Files.write(file.toPath(), "Modified content".getBytes());
                },
                "output.pdf"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Modified content", new String(response.getBody()));
    }

    @Test
    void testProcessPdfFile_ProcessorThrowsIOException() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ResponseEntity<byte[]> response = PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    throw new IOException("Simulated IO error");
                },
                "output.pdf"
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testProcessPdfFile_ProcessorThrowsIllegalArgumentException() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ResponseEntity<byte[]> response = PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    throw new IllegalArgumentException("Invalid parameter");
                },
                "output.pdf"
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testProcessPdfFile_ProcessorThrowsGenericException() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        ResponseEntity<byte[]> response = PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    throw new RuntimeException("Generic error");
                },
                "output.pdf"
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testProcessPdfFile_CleansUpTempFile() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        final File[] capturedFile = new File[1];

        PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    capturedFile[0] = file;
                    assertTrue(file.exists());
                },
                "output.pdf"
        );

        // Verify temp file was cleaned up
        assertFalse(capturedFile[0].exists());
    }

    @Test
    void testProcessPdfFile_CleansUpTempFileEvenOnError() {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        final File[] capturedFile = new File[1];

        PdfFileHelper.processPdfFile(
                pdfFile,
                file -> {
                    capturedFile[0] = file;
                    throw new IOException("Error");
                },
                "output.pdf"
        );

        // Verify temp file was cleaned up even after error
        assertFalse(capturedFile[0].exists());
    }

    @Test
    void testWriteToTempFile_Success() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test content".getBytes()
        );

        File tempFile = PdfFileHelper.writeToTempFile(multipartFile, "test_");

        try {
            assertNotNull(tempFile);
            assertTrue(tempFile.exists());
            assertTrue(tempFile.getName().startsWith("test_"));
            assertTrue(tempFile.getName().endsWith(".pdf"));

            byte[] content = Files.readAllBytes(tempFile.toPath());
            assertEquals("Test content", new String(content));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    void testWriteToTempFile_EmptyFile() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        File tempFile = PdfFileHelper.writeToTempFile(multipartFile, "empty_");

        try {
            assertNotNull(tempFile);
            assertTrue(tempFile.exists());
            assertEquals(0, tempFile.length());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test
    void testWriteToTempFile_LargeFile() throws IOException {
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        File tempFile = PdfFileHelper.writeToTempFile(multipartFile, "large_");

        try {
            assertNotNull(tempFile);
            assertTrue(tempFile.exists());
            assertEquals(largeContent.length, tempFile.length());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
