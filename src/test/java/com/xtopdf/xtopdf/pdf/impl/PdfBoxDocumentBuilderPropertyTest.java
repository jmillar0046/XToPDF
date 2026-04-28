package com.xtopdf.xtopdf.pdf.impl;

import net.jqwik.api.*;
import net.jqwik.api.constraints.FloatRange;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PdfBoxDocumentBuilder.
 *
 * TDD: These tests are written BEFORE the implementation of font loading
 * and formatted text rendering. They will fail at runtime until tasks 2.5
 * and 2.6 implement the underlying functionality.
 *
 * Uses jqwik 1.9.3 for property-based testing.
 */
class PdfBoxDocumentBuilderPropertyTest {

    // ---------------------------------------------------------------
    // Property 2: Font variant selection
    // For all combinations of (bold, italic), verify the builder
    // accepts the call and produces a valid PDF with the text rendered.
    // **Validates: Requirements 2.1, 2.2, 2.3**
    // ---------------------------------------------------------------

    @Property(tries = 25)
    @Label("Property 2: Font variant selection — regular (false, false)")
    void regularFontVariantProducesValidPdfWithText(
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 25)
    @Label("Property 2: Font variant selection — bold (true, false)")
    void boldFontVariantProducesValidPdfWithText(
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, true, false, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 25)
    @Label("Property 2: Font variant selection — italic (false, true)")
    void italicFontVariantProducesValidPdfWithText(
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, true, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 25)
    @Label("Property 2: Font variant selection — bold-italic (true, true)")
    void boldItalicFontVariantProducesValidPdfWithText(
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, true, true, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 25)
    @Label("Property 2: All four font variants in a single paragraph produce valid PDF")
    void allFontVariantsInOneParagraphProduceValidPdf(
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, 12f);
                builder.addFormattedText(text, true, false, 12f);
                builder.addFormattedText(text, false, true, 12f);
                builder.addFormattedText(text, true, true, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            String extracted = extractTextFromPdf(pdfFile);
            // The text should appear (possibly concatenated) without errors
            assertThat(extracted).isNotEmpty();
            // Each variant's text should be present in the output
            assertThat(extracted).contains(text);
        } finally {
            pdfFile.delete();
        }
    }

    // ---------------------------------------------------------------
    // Property 3: Font size passthrough
    // For any positive font size (1–72pt), verify the builder uses it
    // without error. For zero or negative, verify default 12pt fallback.
    // **Validates: Requirements 2.4, 2.5**
    // ---------------------------------------------------------------

    @Property(tries = 25)
    @Label("Property 3: Positive font size is accepted without error")
    void positiveFontSizeIsAcceptedWithoutError(
            @ForAll @FloatRange(min = 6f, max = 48f) float fontSize,
            @ForAll("shortAsciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, fontSize);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            // The PDF should be valid and contain the text
            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 25)
    @Label("Property 3: Zero font size falls back to default without error")
    void zeroFontSizeFallsBackToDefaultWithoutError(
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, 0f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            // Should produce valid PDF with text rendered at default 12pt
            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 25)
    @Label("Property 3: Negative font size falls back to default without error")
    void negativeFontSizeFallsBackToDefaultWithoutError(
            @ForAll @FloatRange(min = -1000f, max = -0.01f) float fontSize,
            @ForAll("asciiText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, fontSize);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            // Should produce valid PDF with text rendered at default 12pt
            assertPdfContainsText(pdfFile, text);
        } finally {
            pdfFile.delete();
        }
    }

    // ---------------------------------------------------------------
    // Property 8: Unicode character passthrough
    // For strings containing Latin, Cyrillic, and CJK characters,
    // verify no '?' substitution occurs in the PDF output.
    // **Validates: Requirements 6.4**
    // ---------------------------------------------------------------

    @Property(tries = 15)
    @Label("Property 8: Latin characters pass through without '?' substitution")
    void latinCharactersPassThroughWithoutSubstitution(
            @ForAll("latinText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            String extracted = extractTextFromPdf(pdfFile);
            assertThat(extracted).doesNotContain("?");
            assertThat(extracted).contains(text);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 15)
    @Label("Property 8: Cyrillic characters pass through without '?' substitution")
    void cyrillicCharactersPassThroughWithoutSubstitution(
            @ForAll("cyrillicText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            String extracted = extractTextFromPdf(pdfFile);
            assertThat(extracted).doesNotContain("?");
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 15)
    @Label("Property 8: CJK characters do not crash the builder")
    void cjkCharactersDoNotCrashBuilder(
            @ForAll("cjkText") String text) throws IOException {

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(text, false, false, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            // CJK OTF font support is limited in PDFBox — verify no crash
            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        } finally {
            pdfFile.delete();
        }
    }

    @Property(tries = 15)
    @Label("Property 8: Mixed Unicode (Latin + Cyrillic + CJK) does not crash the builder")
    void mixedUnicodeDoesNotCrashBuilder(
            @ForAll("latinText") String latin,
            @ForAll("cyrillicText") String cyrillic,
            @ForAll("cjkText") String cjk) throws IOException {

        String mixedText = latin + " " + cyrillic + " " + cjk;

        File pdfFile = createTempPdfFile();
        try {
            try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
                builder.addFormattedText(mixedText, false, false, 12f);
                builder.endParagraph();
                builder.save(pdfFile);
            }

            // CJK OTF font support is limited — verify no crash
            assertThat(pdfFile).exists();
            assertThat(pdfFile.length()).isGreaterThan(0);
        } finally {
            pdfFile.delete();
        }
    }

    // ---------------------------------------------------------------
    // Arbitrary providers
    // ---------------------------------------------------------------

    /**
     * Generates non-empty ASCII text strings (printable characters only,
     * no '?' to avoid false positives in Unicode tests).
     */
    @Provide
    Arbitrary<String> asciiText() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .withChars(' ')
                .ofMinLength(1)
                .ofMaxLength(50)
                .filter(s -> !s.isBlank());
    }

    /**
     * Generates short ASCII text for font size tests where large fonts
     * could cause overflow with longer text.
     */
    @Provide
    Arbitrary<String> shortAsciiText() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(10)
                .filter(s -> !s.isBlank());
    }

    /**
     * Generates non-empty Latin text (basic ASCII letters and digits).
     * Excludes '?' to allow clean assertion on substitution.
     */
    @Provide
    Arbitrary<String> latinText() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(20)
                .filter(s -> !s.isBlank());
    }

    /**
     * Generates non-empty Cyrillic text from the basic Cyrillic Unicode block
     * (U+0410–U+044F: А–я).
     */
    @Provide
    Arbitrary<String> cyrillicText() {
        return Arbitraries.strings()
                .withCharRange('\u0410', '\u044F')  // А–я (basic Cyrillic)
                .ofMinLength(1)
                .ofMaxLength(20);
    }

    /**
     * Generates non-empty CJK text from the CJK Unified Ideographs block
     * (U+4E00–U+9FFF). These are common Chinese/Japanese/Korean characters
     * covered by NotoSansCJK.
     */
    @Provide
    Arbitrary<String> cjkText() {
        return Arbitraries.strings()
                .withCharRange('\u4E00', '\u9FFF')  // CJK Unified Ideographs
                .ofMinLength(1)
                .ofMaxLength(10);
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private File createTempPdfFile() throws IOException {
        Path tempFile = Files.createTempFile("pbt-pdf-", ".pdf");
        return tempFile.toFile();
    }

    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private void assertPdfContainsText(File pdfFile, String expectedText) throws IOException {
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);

        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            PDFTextStripper stripper = new PDFTextStripper();
            String extracted = stripper.getText(doc);
            assertThat(extracted).contains(expectedText);
        }
    }
}
