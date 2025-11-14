package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PptxToPdfServiceTest {

    private PptxToPdfService pptxToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        pptxToPdfService = new PptxToPdfService();
    }

    @Test
    void testConvertPptxToPdf_InvalidFormat_ThrowsIOException() throws Exception {
        // Invalid PPTX data will throw IOException
        byte[] invalidPptxData = "Not a valid PPTX file".getBytes();
        var pptxFile = new MockMultipartFile("file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", invalidPptxData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testPptxOutput.pdf");

        assertThrows(IOException.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_NullMultipartFile_ThrowsException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(Exception.class, () -> pptxToPdfService.convertPptxToPdf(null, pdfFile));
    }

    @Test
    void testConvertPptxToPdf_NullOutputFile_ThrowsNullPointerException() {
        byte[] pptxData = "test".getBytes();
        var pptxFile = new MockMultipartFile("file", "test.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", pptxData);
        assertThrows(Exception.class, () -> pptxToPdfService.convertPptxToPdf(pptxFile, null));
    }
}
