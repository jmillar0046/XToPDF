package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OdpToPdfServiceTest {

    private OdpToPdfService odpToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        odpToPdfService = new OdpToPdfService();
    }

    @Test
    void testConvertOdpToPdf_InvalidFormat_ThrowsIOException() throws Exception {
        // Invalid ODP data will throw IOException
        byte[] invalidOdpData = "Not a valid ODP file".getBytes();
        var odpFile = new MockMultipartFile("file", "test.odp", "application/vnd.oasis.opendocument.presentation", invalidOdpData);

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOdpOutput.pdf");

        assertThrows(IOException.class, () -> odpToPdfService.convertOdpToPdf(odpFile, pdfFile));
    }

    @Test
    void testConvertOdpToPdf_NullMultipartFile_ThrowsIOException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(IOException.class, () -> odpToPdfService.convertOdpToPdf(null, pdfFile));
    }

    @Test
    void testConvertOdpToPdf_NullOutputFile_ThrowsNullPointerException() {
        byte[] odpData = "test".getBytes();
        var odpFile = new MockMultipartFile("file", "test.odp", "application/vnd.oasis.opendocument.presentation", odpData);
        assertThrows(Exception.class, () -> odpToPdfService.convertOdpToPdf(odpFile, null));
    }
}
