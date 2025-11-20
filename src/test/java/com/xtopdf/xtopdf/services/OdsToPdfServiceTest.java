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

class OdsToPdfServiceTest {

    private OdsToPdfService odsToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        odsToPdfService = new OdsToPdfService(pdfBackend);
    }

    @Test
    void testConvertOdsToPdf_ValidFile_Success(@TempDir Path tempDir) throws Exception {
        // Load actual test ODS file
        ClassPathResource resource = new ClassPathResource("test-files/test.ods");
        byte[] odsData = Files.readAllBytes(resource.getFile().toPath());
        
        MockMultipartFile odsFile = new MockMultipartFile(
                "file", 
                "test.ods", 
                "application/vnd.oasis.opendocument.spreadsheet",
                odsData
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        odsToPdfService.convertOdsToPdf(odsFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertOdsToPdf_InvalidFormat_ThrowsIOException(@TempDir Path tempDir) throws Exception {
        // Invalid ODS data will throw IOException
        byte[] invalidOdsData = "Not a valid ODS file".getBytes();
        MockMultipartFile odsFile = new MockMultipartFile(
                "file", 
                "test.ods", 
                "application/vnd.oasis.opendocument.spreadsheet", 
                invalidOdsData
        );

        File pdfFile = tempDir.resolve("testOdsOutput.pdf").toFile();

        assertThrows(IOException.class, () -> odsToPdfService.convertOdsToPdf(odsFile, pdfFile));
    }

    @Test
    void testConvertOdsToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
        MockMultipartFile odsFile = new MockMultipartFile(
                "file", 
                "empty.ods", 
                "application/vnd.oasis.opendocument.spreadsheet",
                new byte[0]
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        assertThrows(IOException.class, () -> odsToPdfService.convertOdsToPdf(odsFile, pdfFile));
    }

    @Test
    void testConvertOdsToPdf_NullMultipartFile_ThrowsIOException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(IOException.class, () -> odsToPdfService.convertOdsToPdf(null, pdfFile));
    }

    @Test
    void testConvertOdsToPdf_NullOutputFile_ThrowsException() {
        byte[] odsData = "test".getBytes();
        MockMultipartFile odsFile = new MockMultipartFile(
                "file", 
                "test.ods", 
                "application/vnd.oasis.opendocument.spreadsheet", 
                odsData
        );
        assertThrows(Exception.class, () -> odsToPdfService.convertOdsToPdf(odsFile, null));
    }

    @Test
    void testConvertOdsToPdf_MultipleSheets_Success(@TempDir Path tempDir) throws Exception {
        // The real test file should have spreadsheet content
        ClassPathResource resource = new ClassPathResource("test-files/test.ods");
        byte[] odsData = Files.readAllBytes(resource.getFile().toPath());
        
        MockMultipartFile odsFile = new MockMultipartFile(
                "file", 
                "multisheet.ods", 
                "application/vnd.oasis.opendocument.spreadsheet",
                odsData
        );

        File pdfFile = tempDir.resolve("multisheet_output.pdf").toFile();

        odsToPdfService.convertOdsToPdf(odsFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}
