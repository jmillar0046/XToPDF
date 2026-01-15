package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TsvToPdfServiceTest {

    private TsvToPdfService tsvToPdfService;

    @BeforeEach
    void setUp() {
        // Use PDFBox backend for testing
        tsvToPdfService = new TsvToPdfService(new PdfBoxBackend());
    }

    @Test
    void testConvertTsvToPdf_SimpleTable_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tAge\tCity\nJohn\t30\tNYC\nJane\t25\tLA";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testTsvOutput.pdf").toFile();

        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);

        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTsvToPdf_EmptyFile_ThrowsIOException(@TempDir Path tempDir) {
        String content = "";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );

        File pdfFile = tempDir.resolve("testTsvEmptyOutput.pdf").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        });
        
        assertEquals("TSV file is empty", exception.getMessage());
    }

    @Test
    void testConvertTsvToPdf_WithQuotedFields_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tDescription\n\"John Doe\"\t\"Test with special chars\"";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "quotes.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("quotesOutput.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTsvToPdf_WithEscapedQuotes_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tDescription\nProduct\t\"Has \"\"quotes\"\"\"";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "escaped.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("escapedOutput.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTsvToPdf_InconsistentColumns_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tAge\tCity\tCountry\nJohn\t30\nJane\t25\tLondon\tUK\nBob\t35\tParis";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "inconsistent.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("inconsistentOutput.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void testConvertTsvToPdf_MultipleColumns_Success(@TempDir Path tempDir) throws Exception {
        String content = "ID\tName\tEmail\tAge\tCity\tCountry\tPhone\tDepartment\n" +
                         "1\tJohn\tjohn@test.com\t30\tNYC\tUSA\t555-1234\tEngineering\n" +
                         "2\tJane\tjane@test.com\t25\tLA\tUSA\t555-5678\tMarketing";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "multi.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("multi_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_LargeDataset_Success(@TempDir Path tempDir) throws Exception {
        StringBuilder content = new StringBuilder("ID\tName\tValue\n");
        for (int i = 0; i < 100; i++) {
            content.append(i).append("\tItem").append(i).append("\t").append(i * 100).append("\n");
        }
        
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "large.tsv", 
                "text/tab-separated-values", 
                content.toString().getBytes()
        );
        File pdfFile = tempDir.resolve("large_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_SpecialCharacters_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tComment\nJohn\tHello 世界\nJane\tÑoño @#$%";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "special.tsv", 
                "text/tab-separated-values", 
                content.getBytes("UTF-8")
        );
        File pdfFile = tempDir.resolve("special_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_EmptyFields_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tAge\tCity\nJohn\t\tNYC\n\t25\t\nJane\t30\tLA";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "empty_fields.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("empty_fields_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_OnlyHeaders_Success(@TempDir Path tempDir) throws Exception {
        String content = "Name\tAge\tCity";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "headers_only.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("headers_only_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_SingleColumn_Success(@TempDir Path tempDir) throws Exception {
        String content = "Names\nJohn\nJane\nBob";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "single.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("single_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_NumericData_Success(@TempDir Path tempDir) throws Exception {
        String content = "ID\tPrice\tQuantity\tTotal\n1\t10.50\t2\t21.00\n2\t5.25\t3\t15.75";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "numeric.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        File pdfFile = tempDir.resolve("numeric_output.pdf").toFile();
        
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        assertTrue(pdfFile.exists());
        assertTrue(pdfFile.length() > 0);
    }

    @Test
    void testConvertTsvToPdf_NullMultipartFile_ThrowsNullPointerException(@TempDir Path tempDir) {
        File pdfFile = tempDir.resolve("nullInput.pdf").toFile();
        assertThrows(NullPointerException.class, 
            () -> tsvToPdfService.convertTsvToPdf(null, pdfFile));
    }

    @Test
    void testConvertTsvToPdf_NullOutputFile_ThrowsIOException() {
        String content = "test";
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "test.tsv", 
                "text/tab-separated-values", 
                content.getBytes()
        );
        assertThrows(IOException.class, 
            () -> tsvToPdfService.convertTsvToPdf(tsvFile, null));
    }

    @Test
    void testParseTsvLine_SimpleFields() {
        String line = "field1\tfield2\tfield3";
        String[] result = tsvToPdfService.parseTsvLine(line, 1);
        
        assertArrayEquals(new String[]{"field1", "field2", "field3"}, result);
    }

    @Test
    void testParseTsvLine_QuotedFieldWithTab() {
        String line = "field1\t\"field\twith\ttab\"\tfield3";
        String[] result = tsvToPdfService.parseTsvLine(line, 1);
        
        assertArrayEquals(new String[]{"field1", "field\twith\ttab", "field3"}, result);
    }

    @Test
    void testParseTsvLine_EscapedQuotes() {
        String line = "field1\t\"field \"\"with\"\" quotes\"\tfield3";
        String[] result = tsvToPdfService.parseTsvLine(line, 1);
        
        assertArrayEquals(new String[]{"field1", "field \"with\" quotes", "field3"}, result);
    }

    @Test
    void testParseTsvLine_EmptyFields() {
        String line = "\t\t";
        String[] result = tsvToPdfService.parseTsvLine(line, 1);
        
        assertArrayEquals(new String[]{"", "", ""}, result);
    }

    @Test
    void testConvertTsvToPdf_ExceedsMaxFileSize_ThrowsIOException(@TempDir Path tempDir) {
        // Create a mock file that reports size larger than MAX_FILE_SIZE (100MB)
        MockMultipartFile largeTsvFile = new MockMultipartFile(
                "file", 
                "large.tsv", 
                "text/tab-separated-values", 
                new byte[0]
        ) {
            @Override
            public long getSize() {
                return 100_000_001L; // Just over 100MB
            }
        };

        File pdfFile = tempDir.resolve("large_output.pdf").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            tsvToPdfService.convertTsvToPdf(largeTsvFile, pdfFile);
        });
        
        assertTrue(exception.getMessage().contains("File size exceeds maximum allowed"));
    }

    @Test
    void testConvertTsvToPdf_ExceedsMaxLineLength_ThrowsIOException(@TempDir Path tempDir) {
        // Create a line that exceeds MAX_LINE_LENGTH (1MB)
        StringBuilder longLine = new StringBuilder();
        for (int i = 0; i < 1_000_001; i++) {
            longLine.append('a');
        }
        
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "long_line.tsv", 
                "text/tab-separated-values", 
                longLine.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("long_line_output.pdf").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        });
        
        assertTrue(exception.getMessage().contains("exceeds maximum length"));
    }

    @Test
    void testConvertTsvToPdf_ExceedsMaxFields_ThrowsIOException(@TempDir Path tempDir) {
        // Create a line with more than MAX_FIELDS (10,000) fields
        StringBuilder manyFields = new StringBuilder();
        for (int i = 0; i < 10_001; i++) {
            if (i > 0) manyFields.append('\t');
            manyFields.append("field").append(i);
        }
        
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file", 
                "many_fields.tsv", 
                "text/tab-separated-values", 
                manyFields.toString().getBytes()
        );

        File pdfFile = tempDir.resolve("many_fields_output.pdf").toFile();

        IOException exception = assertThrows(IOException.class, () -> {
            tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        });
        
        assertTrue(exception.getMessage().contains("exceeds maximum field count"));
    }
}
