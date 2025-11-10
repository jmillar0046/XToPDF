package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlToPdfServiceTest {

    private XmlToPdfService xmlToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        xmlToPdfService = new XmlToPdfService();
    }

    @Test
    void testConvertXmlToPdf_Success() throws Exception {
        var content = "<?xml version=\"1.0\"?><root><item>Test</item></root>";
        var xmlFile = new MockMultipartFile("file", "test.xml", "text/xml", content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testXmlOutput.pdf");

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertXmlToPdf_EmptyFile() throws Exception {
        var content = "";
        var xmlFile = new MockMultipartFile("file", "test.xml", "text/xml", content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testXmlEmptyOutput.pdf");

        xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertXmlToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> xmlToPdfService.convertXmlToPdf(null, pdfFile));
    }

    @Test
    void testConvertXmlToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var xmlFile = new MockMultipartFile("file", "test.xml", "text/xml", content.getBytes());
        assertThrows(IOException.class, () -> xmlToPdfService.convertXmlToPdf(xmlFile, null));
    }
}
