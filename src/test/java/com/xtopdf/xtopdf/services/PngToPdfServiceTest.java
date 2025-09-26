package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PngToPdfServiceTest {

    private PngToPdfService pngToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        pngToPdfService = new PngToPdfService();
    }

    @Test
    void testConvertPngToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> pngToPdfService.convertPngToPdf(null, pdfFile));
    }

    @Test
    void testConvertPngToPdf_NullOutputFile_ThrowsNullPointerException() {
        // Use some dummy PNG data for this test
        byte[] pngData = "dummy".getBytes();
        var pngFile = new MockMultipartFile("file", "test.png", "image/png", pngData);
        assertThrows(NullPointerException.class, () -> pngToPdfService.convertPngToPdf(pngFile, null));
    }

    @Test
    void testConvertPngToPdf_EmptyPngFile() throws Exception {
        var pngFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/emptyPngOutput.pdf");
        
        assertThrows(IOException.class, () -> pngToPdfService.convertPngToPdf(pngFile, pdfFile));
    }

    @Test
    void testConvertPngToPdf_InvalidPngData() throws Exception {
        // Invalid PNG data
        byte[] invalidData = "This is not a PNG file".getBytes();
        var pngFile = new MockMultipartFile("file", "invalid.png", "image/png", invalidData);
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/invalidPngOutput.pdf");
        
        assertThrows(IOException.class, () -> pngToPdfService.convertPngToPdf(pngFile, pdfFile));
    }
}