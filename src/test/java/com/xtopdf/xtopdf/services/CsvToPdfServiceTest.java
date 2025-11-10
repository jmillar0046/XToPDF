package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvToPdfServiceTest {

    private CsvToPdfService csvToPdfService;
    private File pdfFile;

    @BeforeEach
    void setUp() {
        csvToPdfService = new CsvToPdfService();
    }

    @Test
    void testConvertCsvToPdf_Success() throws Exception {
        var content = "Name,Age,City\nJohn,30,NYC\nJane,25,LA";
        var csvFile = new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testCsvOutput.pdf");

        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertCsvToPdf_EmptyFile() throws Exception {
        var content = "";
        var csvFile = new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes());

        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/testCsvEmptyOutput.pdf");

        assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });
    }

    @Test
    void testConvertCsvToPdf_WithQuotes() throws Exception {
        var content = "Name,Description\n\"John Doe\",\"Test, with comma\"";
        var csvFile = new MockMultipartFile("file", "quotes.csv", "text/csv", content.getBytes());
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/quotesOutput.pdf");
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertCsvToPdf_NullMultipartFile_ThrowsNullPointerException() {
        pdfFile = new File(System.getProperty("java.io.tmpdir") + "/nullInput.pdf");
        assertThrows(NullPointerException.class, () -> csvToPdfService.convertCsvToPdf(null, pdfFile));
    }

    @Test
    void testConvertCsvToPdf_NullOutputFile_ThrowsIOException() {
        var content = "test";
        var csvFile = new MockMultipartFile("file", "test.csv", "text/csv", content.getBytes());
        assertThrows(IOException.class, () -> csvToPdfService.convertCsvToPdf(csvFile, null));
    }
}
