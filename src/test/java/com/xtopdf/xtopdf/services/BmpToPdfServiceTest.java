package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BmpToPdfServiceTest {

    private BmpToPdfService bmpToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        bmpToPdfService = new BmpToPdfService();
    }

    @Test
    void testConvertBmpToPdf_InvalidBmp_ThrowsIOException() throws Exception {
        // Invalid BMP will throw IOException
        byte[] bmpData = new byte[] { 0x42, 0x4D, 0x36, 0x00, 0x00, 0x00 };
        var bmpFile = new MockMultipartFile("file", "test.bmp", "image/bmp", bmpData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testBmpOutput.pdf");

        assertThrows(IOException.class, () -> bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile));
    }

    @Test
    void testConvertBmpToPdf_NullMultipartFile_ThrowsException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(Exception.class, () -> bmpToPdfService.convertBmpToPdf(null, pdfFile));
    }

    @Test
    void testConvertBmpToPdf_NullOutputFile_ThrowsException() {
        byte[] bmpData = new byte[] { 0x42, 0x4D, 0x36, 0x00, 0x00, 0x00 };
        var bmpFile = new MockMultipartFile("file", "test.bmp", "image/bmp", bmpData);
        assertThrows(Exception.class, () -> bmpToPdfService.convertBmpToPdf(bmpFile, null));
    }
}
