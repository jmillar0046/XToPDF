package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GifToPdfServiceTest {

    private GifToPdfService gifToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        gifToPdfService = new GifToPdfService(new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend());
    }

    @Test
    void testConvertGifToPdf_Success() throws Exception {
        // Create a minimal 1x1 GIF (GIF89a format)
        byte[] gifData = new byte[] {
            0x47, 0x49, 0x46, 0x38, 0x39, 0x61,  // GIF89a
            0x01, 0x00, 0x01, 0x00,              // Width/Height (1x1)
            (byte)0x80, 0x00, 0x00,              // Global color table flag
            (byte)0xFF, (byte)0xFF, (byte)0xFF,  // Color table
            0x00, 0x00, 0x00,
            0x2C, 0x00, 0x00, 0x00, 0x00,        // Image descriptor
            0x01, 0x00, 0x01, 0x00, 0x00,
            0x02, 0x02, 0x44, 0x01, 0x00,        // Image data
            0x3B                                  // Trailer
        };
        var gifFile = new MockMultipartFile("file", "test.gif", "image/gif", gifData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testGifOutput.pdf");

        gifToPdfService.convertGifToPdf(gifFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertGifToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> gifToPdfService.convertGifToPdf(null, pdfFile));
    }

    @Test
    void testConvertGifToPdf_NullOutputFile_ThrowsIOException() {
        byte[] gifData = new byte[] { 0x47, 0x49, 0x46, 0x38, 0x39, 0x61 };
        var gifFile = new MockMultipartFile("file", "test.gif", "image/gif", gifData);
        assertThrows(IOException.class, () -> gifToPdfService.convertGifToPdf(gifFile, null));
    }
}
