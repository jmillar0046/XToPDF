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

class PptxToPdfServiceTest {

    private PptxToPdfService pptxToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        pptxToPdfService = new PptxToPdfService(pdfBackend);
    }

    @Test
    void testConvertPptxToPdf_ValidFile_Success(@TempDir Path tempDir) throws Exception {
        // Load actual test PPTX file
        ClassPathResource resource = new ClassPathResource("test-files/test.pptx");
        byte[] pptxData = Files.readAllBytes(resource.getFile().toPath());
        
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", 
                "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                pptxData
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertPptxToPdf_InvalidFormat_ThrowsIOException(@TempDir Path tempDir) throws Exception {
        // Invalid PPTX data will throw IOException
        byte[] invalidPptxData = "Not a valid PPTX file".getBytes();
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", 
                "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", 
                invalidPptxData
        );

        File pdfFile = tempDir.resolve("testPptxOutput.pdf").toFile();

        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", 
                "empty.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                new byte[0]
        );

        File pdfFile = tempDir.resolve("output.pdf").toFile();

        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_NullMultipartFile_ThrowsException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(Exception.class, () -> pptxToPdfService.convertPptxToPdf(null, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_NullOutputFile_ThrowsException() {
        byte[] pptxData = "test".getBytes();
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", 
                "test.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation", 
                pptxData
        );
        assertThrows(Exception.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, null));
    }

    @Test
    void testConvertPptxToPdf_LargeFile_Success(@TempDir Path tempDir) throws Exception {
        // Use the real test file which should be relatively large
        ClassPathResource resource = new ClassPathResource("test-files/test.pptx");
        byte[] pptxData = Files.readAllBytes(resource.getFile().toPath());
        
        // Verify file is reasonably sized
        assertTrue(pptxData.length > 10000, "Test file should be at least 10KB");
        
        MockMultipartFile pptxFile = new MockMultipartFile(
                "file", 
                "large.pptx", 
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                pptxData
        );

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }
}
