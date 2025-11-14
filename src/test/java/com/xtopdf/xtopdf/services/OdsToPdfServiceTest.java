package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OdsToPdfServiceTest {

    private OdsToPdfService odsToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        odsToPdfService = new OdsToPdfService();
    }

    @Test
    void testConvertOdsToPdf_InvalidFormat_ThrowsIOException() throws Exception {
        // Invalid ODS data will throw IOException
        byte[] invalidOdsData = "Not a valid ODS file".getBytes();
        var odsFile = new MockMultipartFile("file", "test.ods", "application/vnd.oasis.opendocument.spreadsheet", invalidOdsData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOdsOutput.pdf");

        assertThrows(IOException.class, () -> odsToPdfService.convertOdsToPdf(odsFile, pdfFile));
    }

    @Test
    void testConvertOdsToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> odsToPdfService.convertOdsToPdf(null, pdfFile));
    }

    @Test
    void testConvertOdsToPdf_NullOutputFile_ThrowsNullPointerException() {
        byte[] odsData = "test".getBytes();
        var odsFile = new MockMultipartFile("file", "test.ods", "application/vnd.oasis.opendocument.spreadsheet", odsData);
        assertThrows(Exception.class, () -> odsToPdfService.convertOdsToPdf(odsFile, null));
    }
}
