package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OdtToPdfServiceTest {

    private OdtToPdfService odtToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        odtToPdfService = new OdtToPdfService(pdfBackend);
    }

    @Test
    void testConvertOdtToPdf_ValidFile_Success(@TempDir Path tempDir) throws Exception {
        // Load actual test ODT file
        ClassPathResource resource = new ClassPathResource("test-files/test.odt");
        byte[] odtData = Files.readAllBytes(resource.getFile().toPath());
        
        MockMultipartFile odtFile = new MockMultipartFile(
                "file", 
                "test.odt", 
                "application/vnd.oasis.opendocument.text",
                odtData
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        odtToPdfService.convertOdtToPdf(odtFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertOdtToPdf_InvalidFormat_ThrowsIOException(@TempDir Path tempDir) throws Exception {
        // Invalid ODT data will throw IOException
        byte[] invalidOdtData = "Not a valid ODT file".getBytes();
        MockMultipartFile odtFile = new MockMultipartFile(
                "file", 
                "test.odt", 
                "application/vnd.oasis.opendocument.text", 
                invalidOdtData
        );

        File pdfFile = tempDir.resolve("testOdtOutput.pdf").toFile();

        assertThrows(IOException.class, () -> odtToPdfService.convertOdtToPdf(odtFile, pdfFile));
    }

    @Test
    void testConvertOdtToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
        MockMultipartFile odtFile = new MockMultipartFile(
                "file", 
                "empty.odt", 
                "application/vnd.oasis.opendocument.text",
                new byte[0]
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        assertThrows(IOException.class, () -> odtToPdfService.convertOdtToPdf(odtFile, pdfFile));
    }

    @Test
    void testConvertOdtToPdf_NullMultipartFile_ThrowsIOException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(IOException.class, () -> odtToPdfService.convertOdtToPdf(null, pdfFile));
    }

    @Test
    void testConvertOdtToPdf_NullOutputFile_ThrowsException() {
        byte[] odtData = "test".getBytes();
        MockMultipartFile odtFile = new MockMultipartFile(
                "file", 
                "test.odt", 
                "application/vnd.oasis.opendocument.text", 
                odtData
        );
        assertThrows(Exception.class, () -> odtToPdfService.convertOdtToPdf(odtFile, null));
    }

    @Test
    void testConvertOdtToPdf_FormattedDocument_Success(@TempDir Path tempDir) throws Exception {
        // The real test file should have formatted text content
        ClassPathResource resource = new ClassPathResource("test-files/test.odt");
        byte[] odtData = Files.readAllBytes(resource.getFile().toPath());
        
        MockMultipartFile odtFile = new MockMultipartFile(
                "file", 
                "formatted.odt", 
                "application/vnd.oasis.opendocument.text",
                odtData
        );

        File pdfFile = tempDir.resolve("formatted_output.pdf").toFile();

        odtToPdfService.convertOdtToPdf(odtFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}
