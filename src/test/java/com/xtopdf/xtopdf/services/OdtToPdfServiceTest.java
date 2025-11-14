package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OdtToPdfServiceTest {

    private OdtToPdfService odtToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        odtToPdfService = new OdtToPdfService();
    }

    @Test
    void testConvertOdtToPdf_InvalidFormat_ThrowsIOException() throws Exception {
        // Invalid ODT data will throw IOException
        byte[] invalidOdtData = "Not a valid ODT file".getBytes();
        var odtFile = new MockMultipartFile("file", "test.odt", "application/vnd.oasis.opendocument.text", invalidOdtData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOdtOutput.pdf");

        assertThrows(IOException.class, () -> odtToPdfService.convertOdtToPdf(odtFile, pdfFile));
    }

    @Test
    void testConvertOdtToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> odtToPdfService.convertOdtToPdf(null, pdfFile));
    }

    @Test
    void testConvertOdtToPdf_NullOutputFile_ThrowsNullPointerException() {
        byte[] odtData = "test".getBytes();
        var odtFile = new MockMultipartFile("file", "test.odt", "application/vnd.oasis.opendocument.text", odtData);
        assertThrows(Exception.class, () -> odtToPdfService.convertOdtToPdf(odtFile, null));
    }
}
