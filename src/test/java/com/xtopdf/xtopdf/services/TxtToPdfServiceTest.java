package com.xtopdf.xtopdf.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TxtToPdfServiceTest {

    private TxtToPdfService txtToPdfService;

    private File txtFile;

    private File pdfFile;

    @BeforeEach
    void setUp() {
        txtToPdfService = new TxtToPdfService();
    }


    @Test
    void testConvertTxtToPdf_Success() throws Exception {
        var content = "Hello, this is a test file content!";
        var txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.pdf");

        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_EmptyFile() throws Exception {
        var content = "";
        var txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testEmptyOutput.pdf");

        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertTxtToPdf_InvalidPdfCreation() throws Exception {
        var content = "Hello, this is a test file content!";
        var txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());

        assertThrows(IOException.class, () -> {
            txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        });
    }

    @Test
    void testConvertTxtToPdf_WithUnicodeCharacters() throws Exception {
        var content = "Hello, 你好, привет, 😀";
        var txtFile = new MockMultipartFile("file", "unicode.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes("UTF-8"));
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/unicodeOutput.pdf");
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTxtToPdf_OnlyWhitespace() throws Exception {
        var content = "     \n   \n";
        var txtFile = new MockMultipartFile("file", "whitespace.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/whitespaceOutput.pdf");
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
    }

    @Test
    void testConvertTxtToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> txtToPdfService.convertTxtToPdf(null, pdfFile));
    }

    @Test
    void testConvertTxtToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var txtFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        assertThrows(IOException.class, () -> txtToPdfService.convertTxtToPdf(txtFile, null));
    }

    @Test
    void testConvertTxtToPdf_MultipleLines() throws Exception {
        var content = "Line 1\nLine 2\nLine 3";
        var txtFile = new MockMultipartFile("file", "multilines.txt", MediaType.TEXT_PLAIN_VALUE, content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/multilinesOutput.pdf");
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }
}
