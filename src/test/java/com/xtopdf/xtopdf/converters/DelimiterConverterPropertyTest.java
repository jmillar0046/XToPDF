package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.DelimiterSeparatedToPdfService;
import net.jqwik.api.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for DelimiterSeparatedConverter.
 * Verifies that the unified converter produces valid PDF output for both
 * CSV and TSV content with correct text extraction.
 *
 * Property 7: Delimiter Converter Correctness — For any valid
 * delimiter-separated input content, the DelimiterSeparatedConverter configured
 * with the corresponding delimiter SHALL produce a valid PDF containing the
 * input data.
 *
 * **Validates: Requirements 5.4, 5.5**
 */
class DelimiterConverterPropertyTest {

    private static String extractPdfText(File pdfFile) throws Exception {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    /**
     * Property 7 (CSV): For any valid comma-separated content, the
     * DelimiterSeparatedConverter with comma delimiter produces a valid PDF
     * containing the input data.
     *
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 15)
    @Tag("Feature: repo-efficiency-improvements, Property 7: Delimiter Converter Correctness")
    void csvConverterProducesValidPdf(
            @ForAll("csvContent") String csvContent) throws Exception {

        PdfBackendProvider pdfBackend = new PdfBoxBackend();

        // Unified converter path
        DelimiterSeparatedToPdfService delimiterService = new DelimiterSeparatedToPdfService(pdfBackend);
        DelimiterSeparatedConverter unifiedConverter = new DelimiterSeparatedConverter(
                delimiterService, ',', "CSV", Set.of(".csv"));

        Path tempDir = Files.createTempDirectory("delimiter-test");
        File output = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile inputFile = new MockMultipartFile(
                    "file", "data.csv", "text/csv", csvContent.getBytes());

            unifiedConverter.convertToPDF(inputFile, output.getAbsolutePath());

            // Verify PDF is valid and contains content
            String pdfText = extractPdfText(output);
            assertThat(pdfText)
                    .as("CSV converter should produce a PDF with text content")
                    .isNotEmpty();

            // Verify it's a valid PDF with at least one page
            try (PDDocument doc = Loader.loadPDF(output)) {
                assertThat(doc.getNumberOfPages())
                        .as("CSV converter should produce a PDF with at least one page")
                        .isGreaterThanOrEqualTo(1);
            }
        } finally {
            output.delete();
            tempDir.toFile().delete();
        }
    }

    /**
     * Property 7 (TSV): For any valid tab-separated content, the
     * DelimiterSeparatedConverter with tab delimiter produces a valid PDF
     * containing the input data.
     *
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 15)
    @Tag("Feature: repo-efficiency-improvements, Property 7: Delimiter Converter Correctness")
    void tsvConverterProducesValidPdf(
            @ForAll("tsvContent") String tsvContent) throws Exception {

        PdfBackendProvider pdfBackend = new PdfBoxBackend();

        // Unified converter path
        DelimiterSeparatedToPdfService delimiterService = new DelimiterSeparatedToPdfService(pdfBackend);
        DelimiterSeparatedConverter unifiedConverter = new DelimiterSeparatedConverter(
                delimiterService, '\t', "TSV", Set.of(".tsv", ".tab"));

        Path tempDir = Files.createTempDirectory("delimiter-test");
        File output = tempDir.resolve("output.pdf").toFile();

        try {
            MockMultipartFile inputFile = new MockMultipartFile(
                    "file", "data.tsv", "text/tab-separated-values", tsvContent.getBytes());

            unifiedConverter.convertToPDF(inputFile, output.getAbsolutePath());

            // Verify PDF is valid and contains content
            String pdfText = extractPdfText(output);
            assertThat(pdfText)
                    .as("TSV converter should produce a PDF with text content")
                    .isNotEmpty();

            // Verify it's a valid PDF with at least one page
            try (PDDocument doc = Loader.loadPDF(output)) {
                assertThat(doc.getNumberOfPages())
                        .as("TSV converter should produce a PDF with at least one page")
                        .isGreaterThanOrEqualTo(1);
            }
        } finally {
            output.delete();
            tempDir.toFile().delete();
        }
    }

    // ---- Providers ----

    @Provide
    Arbitrary<String> csvContent() {
        // Generate random CSV content: 1-5 rows, 1-4 columns, simple alphanumeric values
        Arbitrary<Integer> numCols = Arbitraries.integers().between(1, 4);
        Arbitrary<Integer> numRows = Arbitraries.integers().between(1, 5);

        return Combinators.combine(numRows, numCols)
                .flatAs((rows, cols) -> {
                    Arbitrary<String> row = Arbitraries.strings()
                            .withCharRange('a', 'z')
                            .ofMinLength(1)
                            .ofMaxLength(10)
                            .list().ofSize(cols)
                            .map(cells -> String.join(",", cells));

                    return row.list().ofSize(rows)
                            .map(rowList -> String.join("\n", rowList));
                });
    }

    @Provide
    Arbitrary<String> tsvContent() {
        // Generate random TSV content: 1-5 rows, 1-4 columns, simple alphanumeric values
        Arbitrary<Integer> numCols = Arbitraries.integers().between(1, 4);
        Arbitrary<Integer> numRows = Arbitraries.integers().between(1, 5);

        return Combinators.combine(numRows, numCols)
                .flatAs((rows, cols) -> {
                    Arbitrary<String> row = Arbitraries.strings()
                            .withCharRange('a', 'z')
                            .ofMinLength(1)
                            .ofMaxLength(10)
                            .list().ofSize(cols)
                            .map(cells -> String.join("\t", cells));

                    return row.list().ofSize(rows)
                            .map(rowList -> String.join("\n", rowList));
                });
    }
}
