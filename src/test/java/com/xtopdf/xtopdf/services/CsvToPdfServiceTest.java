package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvToPdfServiceTest {

    private CsvToPdfService csvToPdfService;

    @BeforeEach
    void setUp() {
        // Use PDFBox backend for testing
        csvToPdfService = new CsvToPdfService(new PdfBoxBackend());
    }

    @Test
    void testConvertCsvToPdf_SimpleTable_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name,Age,City\nJohn,30,NYC\nJane,25,LA";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "test.csv", 
                "text/csv", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testCsvOutput.pdf").toFile();

        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertCsvToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
        String content = "";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "test.csv", 
                "text/csv", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testCsvEmptyOutput.pdf").toFile();

        assertThrows(IOException.class, () -> {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        });
    }

    @Test
    void testConvertCsvToPdf_WithQuotes_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name,Description\n\"John Doe\",\"Test, with comma\"";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "quotes.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("quotesOutput.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertCsvToPdf_MultipleColumns_Success(@TempDir Path tempDir) throws Exception {
        String content = "ID,Name,Email,Age,City,Country,Phone,Department\n" +
                         "1,John,john@test.com,30,NYC,USA,555-1234,Engineering\n" +
                         "2,Jane,jane@test.com,25,LA,USA,555-5678,Marketing";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "multi.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("multi_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_LargeDataset_Success(@TempDir Path tempDir) throws Exception {
        StringBuilder content = new StringBuilder("ID,Name,Value\n");
        for (int i = 0; i < 100; i++) {
            content.append(i).append(",Item").append(i).append(",").append(i * 100).append("\n");
        }
        
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "large.csv", 
                "text/csv", 
                content.toString().getBytes()
        );
        File pdfFile = tempDir.resolve("large_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_SpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name,Comment\nJohn,Hello 世界\nJane,Ñoño @#$%";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "special.csv", 
                "text/csv", 
                content.getBytes("UTF-8")
        );
        File pdfFile = tempDir.resolve("special_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_EmptyFields_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name,Age,City\nJohn,,NYC\n,25,\nJane,30,LA";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "empty_fields.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("empty_fields_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_OnlyHeaders_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name,Age,City";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "headers_only.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("headers_only_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_SingleColumn_Success(@TempDir Path tempDir) throws Exception {
        String content = "Names\nJohn\nJane\nBob";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "single.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("single_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_NumericData_Success(@TempDir Path tempDir) throws Exception {
        String content = "ID,Price,Quantity,Total\n1,10.50,2,21.00\n2,5.25,3,15.75";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "numeric.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("numeric_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_WithNewlines_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name,Description\n\"John\",\"Line1\nLine2\"\n\"Jane\",\"Single line\"";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "newlines.csv", 
                "text/csv", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("newlines_output.pdf").toFile();
        
        csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertCsvToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(NullPointerException.class, 
            () -> csvToPdfService.convertCsvToPdf(null, pdfFile));
    }

    @Test
    void testConvertCsvToPdf_NullOutputFile_ThrowsIOException() {
        String content = "test";
        MockMultipartFile csvFile = new MockMultipartFile(
                "file", 
                "test.csv", 
                "text/csv", 
                content.getBytes()
        );
        assertThrows(IOException.class, 
            () -> csvToPdfService.convertCsvToPdf(csvFile, null));
    }
}
