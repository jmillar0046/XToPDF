package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class IgesToPdfServiceTest {

    private IgesToPdfService igesToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        igesToPdfService = new IgesToPdfService(pdfBackend);
    }

    @Test
    void testConvertIgesToPdf_Success() throws Exception {
        String content = String.format("%-72sS%7d\n%-72sG%7d\n%-72sD%7d\n%-72sP%7d\n%-72sT%7d\n",
                "Start Section", 1, "Global Section", 1, "Directory", 1, "Parameter", 1, "Terminate", 1);
        
        MockMultipartFile igesFile = new MockMultipartFile(
                "file", "test.iges", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        File pdfFile = tempDir.resolve("testIgesOutput.pdf").toFile();

        igesToPdfService.convertIgesToPdf(igesFile, pdfFile);

        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertIgesToPdf_EmptyFile() throws Exception {
        MockMultipartFile igesFile = new MockMultipartFile(
                "file", "test.iges", MediaType.APPLICATION_OCTET_STREAM_VALUE, "".getBytes());

        File pdfFile = tempDir.resolve("testEmptyIgesOutput.pdf").toFile();

        igesToPdfService.convertIgesToPdf(igesFile, pdfFile);

        assertTrue(pdfFile.exists());
    }
}
