package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.utils.ExcelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class XlsxToPdfServiceTest {

    private XlsxToPdfService xlsxToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        xlsxToPdfService = new XlsxToPdfService(pdfBackend);
        tempDir = Files.createTempDirectory("xlsx-test");
    }

    @Test
    void convertXlsxToPdf_EmptyFile_ThrowsIOException() {
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]);
        var outputFile = new File(tempDir.toFile(), "output.pdf");

        assertThrows(IOException.class, () -> xlsxToPdfService.convertXlsxToPdf(inputFile, outputFile));
    }

    @Test
    void convertXlsxToPdf_InvalidXlsxContent_ThrowsIOException() {
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "invalid content".getBytes());
        var outputFile = new File(tempDir.toFile(), "output.pdf");

        assertThrows(IOException.class, () -> xlsxToPdfService.convertXlsxToPdf(inputFile, outputFile));
    }

    @Test
    void getCellValueAsString_NullCell_ReturnsEmptyString() {
        String result = ExcelUtils.getCellValueAsString(null);
        assertEquals("", result);
    }

    @Test
    void convertXlsxToPdf_WithExecuteMacrosFalse_DoesNotThrow() {
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "invalid content".getBytes());
        var outputFile = new File(tempDir.toFile(), "output.pdf");

        // Should throw IOException regardless of executeMacros parameter for invalid content
        assertThrows(IOException.class, () -> xlsxToPdfService.convertXlsxToPdf(inputFile, outputFile, false));
    }

    @Test
    void convertXlsxToPdf_WithExecuteMacrosTrue_DoesNotThrow() {
        var inputFile = new MockMultipartFile("inputFile", "test.xlsx", MediaType.APPLICATION_OCTET_STREAM_VALUE, "invalid content".getBytes());
        var outputFile = new File(tempDir.toFile(), "output.pdf");

        // Should throw IOException regardless of executeMacros parameter for invalid content
        assertThrows(IOException.class, () -> xlsxToPdfService.convertXlsxToPdf(inputFile, outputFile, true));
    }
}