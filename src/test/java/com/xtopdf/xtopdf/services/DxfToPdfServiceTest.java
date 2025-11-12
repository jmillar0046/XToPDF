package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DxfToPdfServiceTest {

    private DxfToPdfService dxfToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        dxfToPdfService = new DxfToPdfService();
    }

    @Test
    void testConvertDxfToPdf_Success() throws Exception {
        // Create a simple DXF content (basic DXF header)
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testDxfOutput.pdf");

        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_EmptyFile() throws Exception {
        var content = "";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyDxfOutput.pdf");

        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_InvalidPdfCreation() {
        var content = "Test DXF content";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        assertThrows(IOException.class, () -> {
            dxfToPdfService.convertDxfToPdf(dxfFile, null);
        });
    }

    @Test
    void testConvertDxfToPdf_WithComplexContent() throws Exception {
        var content = "0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n0\nSECTION\n2\nENTITIES\n0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "complex.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/complexDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }

    @Test
    void testConvertDxfToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> dxfToPdfService.convertDxfToPdf(null, pdfFile));
    }

    @Test
    void testConvertDxfToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        assertThrows(IOException.class, () -> dxfToPdfService.convertDxfToPdf(dxfFile, null));
    }

    @Test
    void testConvertDxfToPdf_MultipleLines() throws Exception {
        var content = "Line 1\nLine 2\nLine 3";
        var dxfFile = new MockMultipartFile("file", "multilines.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());
        
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/multilinesDxfOutput.pdf");
        
        dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
        
        // Clean up
        pdfFile.delete();
    }
}
