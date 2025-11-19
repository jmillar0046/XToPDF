package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PptToPdfServiceTest {

    private PptToPdfService pptToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        pptToPdfService = new PptToPdfService();
    }

    @Test
    void testConvertPptToPdf_InvalidFormat_ThrowsIOException() throws Exception {
        // Invalid PPT data will throw IOException
        byte[] invalidPptData = "Not a valid PPT file".getBytes();
        var pptFile = new MockMultipartFile("file", "test.ppt", "application/vnd.ms-powerpoint", invalidPptData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testPptOutput.pdf");

        assertThrows(IOException.class, () -> pptToPdfService.convertPptToPdf(pptFile, pdfFile));
    }

    @Test
    void testConvertPptToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> pptToPdfService.convertPptToPdf(null, pdfFile));
    }

    @Test
    void testConvertPptToPdf_NullOutputFile_ThrowsNullPointerException() {
        byte[] pptData = "test".getBytes();
        var pptFile = new MockMultipartFile("file", "test.ppt", "application/vnd.ms-powerpoint", pptData);
        assertThrows(Exception.class, () -> pptToPdfService.convertPptToPdf(pptFile, null));
    }
}
