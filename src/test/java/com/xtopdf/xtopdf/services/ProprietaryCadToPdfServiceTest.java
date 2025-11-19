package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ProprietaryCadToPdfServiceTest {

    private ProprietaryCadToPdfService proprietaryCadToPdfService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        proprietaryCadToPdfService = new ProprietaryCadToPdfService();
    }

    @Test
    void testConvertToPdf_WithSuggestions() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.sldprt", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        File pdfFile = tempDir.resolve("testProprietaryOutput.pdf").toFile();
        
        String[] suggestions = {"Use native software", "Export to STEP format"};
        
        proprietaryCadToPdfService.convertToPdf(inputFile, pdfFile, "Test Format", "TEST", suggestions);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertToPdf_NoSuggestions() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "file", "test.file", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy content".getBytes());

        File pdfFile = tempDir.resolve("testProprietaryNoSuggestionsOutput.pdf").toFile();
        
        proprietaryCadToPdfService.convertToPdf(inputFile, pdfFile, "Test Format", "TEST", null);

        assertTrue(pdfFile.exists());
    }
}
