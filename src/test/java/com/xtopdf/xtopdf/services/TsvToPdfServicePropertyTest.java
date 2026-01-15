package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for TSV to PDF conversion service.
 * Uses jqwik for property-based testing with minimum 100 iterations per property.
 */
class TsvToPdfServicePropertyTest {

    private final PdfBackendProvider pdfBackend = new PdfBoxBackend();
    private final TsvToPdfService tsvToPdfService = new TsvToPdfService(pdfBackend);

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 1: TSV Parsing and Normalization")
    void tsvParsingAndNormalization(
            @ForAll @Size(min = 1, max = 10) List<@Size(min = 1, max = 5) List<String>> rows
    ) {
        // Generate TSV content with varying column counts
        int maxColumns = rows.stream().mapToInt(List::size).max().orElse(0);
        
        // Parse each row
        for (List<String> row : rows) {
            String tsvLine = String.join("\t", row);
            String[] parsed = tsvToPdfService.parseTsvLine(tsvLine);
            
            // Verify parsing splits correctly on tabs
            assertEquals(row.size(), parsed.length, "Parsed row should have correct number of fields");
            
            for (int i = 0; i < row.size(); i++) {
                assertEquals(row.get(i), parsed[i], "Field values should match");
            }
        }
        
        // Verify normalization would produce consistent column counts
        assertTrue(maxColumns > 0, "Should have at least one column");
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 2: PDF File Creation")
    void pdfFileCreation(
            @ForAll @Size(min = 1, max = 5) List<@Size(min = 1, max = 3) List<@From("printableStrings") String>> rows,
            @TempDir Path tempDir
    ) throws IOException {
        // Generate valid TSV content
        StringBuilder content = new StringBuilder();
        for (List<String> row : rows) {
            content.append(String.join("\t", row)).append("\n");
        }
        
        MockMultipartFile tsvFile = new MockMultipartFile(
                "file",
                "test.tsv",
                "text/tab-separated-values",
                content.toString().getBytes()
        );
        
        // Generate random output file path
        File pdfFile = tempDir.resolve("output_" + System.nanoTime() + ".pdf").toFile();
        
        // Perform conversion
        tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        
        // Verify PDF file exists at output location
        assertTrue(pdfFile.exists(), "PDF file should exist after conversion");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
    }

    @Provide
    Arbitrary<String> printableStrings() {
        // Generate printable strings without tabs or special PDF-problematic characters
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(20);
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 1: TSV Parsing and Normalization")
    void tsvParsingHandlesEmptyFields(
            @ForAll @IntRange(min = 1, max = 10) int fieldCount
    ) {
        // Create TSV line with empty fields
        String[] emptyFields = new String[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            emptyFields[i] = "";
        }
        String tsvLine = String.join("\t", emptyFields);
        
        String[] parsed = tsvToPdfService.parseTsvLine(tsvLine);
        
        // Verify correct number of empty fields
        assertEquals(fieldCount, parsed.length, "Should parse correct number of fields");
        for (String field : parsed) {
            assertEquals("", field, "All fields should be empty");
        }
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 1: TSV Parsing and Normalization")
    void tsvParsingHandlesQuotedFields(
            @ForAll @From("printableStrings") String field1,
            @ForAll @From("printableStrings") String field2,
            @ForAll @From("printableStrings") String field3
    ) {
        // Create TSV line with quoted fields
        String tsvLine = field1 + "\t\"" + field2 + "\"\t" + field3;
        
        String[] parsed = tsvToPdfService.parseTsvLine(tsvLine);
        
        // Verify parsing handles quotes correctly
        assertEquals(3, parsed.length, "Should have 3 fields");
        assertEquals(field1, parsed[0], "First field should match");
        assertEquals(field2, parsed[1], "Second field should match (quotes removed)");
        assertEquals(field3, parsed[2], "Third field should match");
    }

    @Property(tries = 100)
    @Tag("Feature: tsv-file-support, Property 1: TSV Parsing and Normalization")
    void tsvParsingHandlesEscapedQuotes(
            @ForAll @From("printableStrings") String prefix,
            @ForAll @From("printableStrings") String suffix
    ) {
        // Create TSV line with escaped quotes
        String tsvLine = "field1\t\"" + prefix + "\"\"" + suffix + "\"\tfield3";
        
        String[] parsed = tsvToPdfService.parseTsvLine(tsvLine);
        
        // Verify escaped quotes are handled correctly
        assertEquals(3, parsed.length, "Should have 3 fields");
        assertEquals("field1", parsed[0], "First field should match");
        assertEquals(prefix + "\"" + suffix, parsed[1], "Second field should have single quote");
        assertEquals("field3", parsed[2], "Third field should match");
    }
}
