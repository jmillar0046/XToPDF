package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonToPdfServiceTest {

    private JsonToPdfService jsonToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        jsonToPdfService = new JsonToPdfService();
    }

    @Test
    void testConvertJsonToPdf_Success() throws Exception {
        var content = "{\"name\":\"John\",\"age\":30}";
        var jsonFile = new MockMultipartFile("file", "test.json", "application/json", content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testJsonOutput.pdf");

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertJsonToPdf_EmptyFile() throws Exception {
        var content = "";
        var jsonFile = new MockMultipartFile("file", "test.json", "application/json", content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testJsonEmptyOutput.pdf");

        jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertJsonToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> jsonToPdfService.convertJsonToPdf(null, pdfFile));
    }

    @Test
    void testConvertJsonToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var jsonFile = new MockMultipartFile("file", "test.json", "application/json", content.getBytes());
        assertThrows(IOException.class, () -> jsonToPdfService.convertJsonToPdf(jsonFile, null));
    }
}
