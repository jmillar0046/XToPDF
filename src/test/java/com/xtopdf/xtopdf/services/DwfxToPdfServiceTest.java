package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DwfxToPdfServiceTest {

    private DwfxToPdfService dwfxToPdfService;
    private DwfToPdfService dwfToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dwfToPdfService = new DwfToPdfService();
        dwfToPdfService.pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        dwfxToPdfService = new DwfxToPdfService(dwfToPdfService);
    }

    @Test
    void testConvertDwfxToPdf() throws Exception {
        // Create empty file content - DWFX service should handle gracefully
        byte[] emptyContent = new byte[0];
        
        MockMultipartFile inputFile = new MockMultipartFile("file", "test.dwfx", "application/octet-stream", emptyContent);
        File outputFile = tempDir.resolve("output.pdf").toFile();

        dwfxToPdfService.convertDwfxToPdf(inputFile, outputFile);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
