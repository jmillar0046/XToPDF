package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.CsvToPdfService;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.DelimiterSeparatedToPdfService;
import com.xtopdf.xtopdf.services.conversion.spreadsheet.TsvToPdfService;
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
 * Property-based tests for DelimiterSeparatedConverter backward compatibility.
 * Verifies that the unified converter produces identical PDF text content to the
 * original dedicated CsvFileConverter and TsvFileConverter.
 *
 * Property 7: Delimiter Converter Backward Compatibility — For any valid
 * delimiter-separated input content, the DelimiterSeparatedConverter configured
 * with the corresponding delimiter SHALL produce PDF output byte-equivalent to
 * the original dedicated converter.
 *
 * Note: PDFBox generates unique document IDs per PDF, so we compare extracted
 * text content rather than raw bytes. This validates that the conversion logic
 * produces semantically identical output.
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
     * DelimiterSeparatedConverter with comma delimiter produces PDF output
     * with text content identical to the original CsvFileConverter.
     *
     * **Validates: Requirements 5.4**
     */
    @Property(tries = 50)
    @Tag("Feature: repo-efficiency-improvements, Property 7: Delimiter Converter Backward Compatibility")
    void csvConverterProducesSameOutputAsOriginal(
            @ForAll("csvContent") String csvContent) throws Exception {

        PdfBackendProvider pdfBackend = new PdfBoxBackend();

        // Original converter path
        CsvToPdfService csvService = new CsvToPdfService(pdfBackend);
        CsvFileConverter originalConverter = new CsvFileConverter(csvService);

        // New unified converter path
        DelimiterSeparatedToPdfService delimiterService = new DelimiterSeparatedToPdfService(pdfBackend);
        DelimiterSeparatedConverter unifiedConverter = new DelimiterSeparatedConverter(
                delimiterService, ',', "CSV", Set.of(".csv"));

        Path tempDir = Files.createTempDirectory("delimiter-test");
        File originalOutput = tempDir.resolve("original.pdf").toFile();
        File unifiedOutput = tempDir.resolve("unified.pdf").toFile();

        try {
            MockMultipartFile inputFile1 = new MockMultipartFile(
                    "file", "data.csv", "text/csv", csvContent.getBytes());
            MockMultipartFile inputFile2 = new MockMultipartFile(
                    "file", "data.csv", "text/csv", csvContent.getBytes());

            originalConverter.convertToPDF(inputFile1, originalOutput.getAbsolutePath());
            unifiedConverter.convertToPDF(inputFile2, unifiedOutput.getAbsolutePath());

            String originalText = extractPdfText(originalOutput);
            String unifiedText = extractPdfText(unifiedOutput);

            assertThat(unifiedText)
                    .as("Unified CSV converter should produce same text content as original")
                    .isEqualTo(originalText);

            // Also verify both produce valid PDFs with same page count
            try (PDDocument origDoc = Loader.loadPDF(originalOutput);
                 PDDocument unifiedDoc = Loader.loadPDF(unifiedOutput)) {
                assertThat(unifiedDoc.getNumberOfPages())
                        .as("Unified CSV converter should produce same number of pages")
                        .isEqualTo(origDoc.getNumberOfPages());
            }
        } finally {
            originalOutput.delete();
            unifiedOutput.delete();
            tempDir.toFile().delete();
        }
    }

    /**
     * Property 7 (TSV): For any valid tab-separated content, the
     * DelimiterSeparatedConverter with tab delimiter produces PDF output
     * with text content identical to the original TsvFileConverter.
     *
     * **Validates: Requirements 5.5**
     */
    @Property(tries = 50)
    @Tag("Feature: repo-efficiency-improvements, Property 7: Delimiter Converter Backward Compatibility")
    void tsvConverterProducesSameOutputAsOriginal(
            @ForAll("tsvContent") String tsvContent) throws Exception {

        PdfBackendProvider pdfBackend = new PdfBoxBackend();

        // Original converter path
        TsvToPdfService tsvService = new TsvToPdfService(pdfBackend);
        TsvFileConverter originalConverter = new TsvFileConverter(tsvService);

        // New unified converter path
        DelimiterSeparatedToPdfService delimiterService = new DelimiterSeparatedToPdfService(pdfBackend);
        DelimiterSeparatedConverter unifiedConverter = new DelimiterSeparatedConverter(
                delimiterService, '\t', "TSV", Set.of(".tsv", ".tab"));

        Path tempDir = Files.createTempDirectory("delimiter-test");
        File originalOutput = tempDir.resolve("original.pdf").toFile();
        File unifiedOutput = tempDir.resolve("unified.pdf").toFile();

        try {
            MockMultipartFile inputFile1 = new MockMultipartFile(
                    "file", "data.tsv", "text/tab-separated-values", tsvContent.getBytes());
            MockMultipartFile inputFile2 = new MockMultipartFile(
                    "file", "data.tsv", "text/tab-separated-values", tsvContent.getBytes());

            originalConverter.convertToPDF(inputFile1, originalOutput.getAbsolutePath());
            unifiedConverter.convertToPDF(inputFile2, unifiedOutput.getAbsolutePath());

            String originalText = extractPdfText(originalOutput);
            String unifiedText = extractPdfText(unifiedOutput);

            assertThat(unifiedText)
                    .as("Unified TSV converter should produce same text content as original")
                    .isEqualTo(originalText);

            // Also verify both produce valid PDFs with same page count
            try (PDDocument origDoc = Loader.loadPDF(originalOutput);
                 PDDocument unifiedDoc = Loader.loadPDF(unifiedOutput)) {
                assertThat(unifiedDoc.getNumberOfPages())
                        .as("Unified TSV converter should produce same number of pages")
                        .isEqualTo(origDoc.getNumberOfPages());
            }
        } finally {
            originalOutput.delete();
            unifiedOutput.delete();
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
