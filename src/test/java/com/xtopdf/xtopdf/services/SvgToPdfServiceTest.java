package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SvgToPdfServiceTest {

    private SvgToPdfService svgToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        svgToPdfService = new SvgToPdfService();
    }

    @Test
    void testConvertSvgToPdf_Success() throws Exception {
        // Create a minimal valid SVG
        String svgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\">\n" +
                "  <rect width=\"100\" height=\"100\" fill=\"blue\"/>\n" +
                "</svg>";
        byte[] svgData = svgContent.getBytes();
        var svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svgData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testSvgOutput.pdf");

        svgToPdfService.convertSvgToPdf(svgFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertSvgToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> svgToPdfService.convertSvgToPdf(null, pdfFile));
    }

    @Test
    void testConvertSvgToPdf_NullOutputFile_ThrowsIOException() {
        String svgContent = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>";
        var svgFile = new MockMultipartFile("file", "test.svg", "image/svg+xml", svgContent.getBytes());
        assertThrows(IOException.class, () -> svgToPdfService.convertSvgToPdf(svgFile, null));
    }
}
