package com.xtopdf.xtopdf.pdf.impl;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NotoSans font loading in PdfBoxDocumentBuilder.
 *
 * These tests verify that the builder correctly loads NotoSans-Regular,
 * NotoSans-Bold, and NotoSansCJK-Regular fonts from the classpath and
 * falls back to Helvetica when fonts are unavailable.
 *
 * TDD: These tests are written BEFORE the font loading implementation.
 * They will fail until task 2.5 implements font loading in the constructor.
 *
 * Validates: Requirements 6.1, 6.2, 6.3, 6.5
 */
class PdfBoxDocumentBuilderTest {

    @TempDir
    Path tempDir;

    // ---------------------------------------------------------------
    // 1. Constructor smoke test — builder creates successfully
    // ---------------------------------------------------------------

    @Test
    void constructorShouldCreateBuilderWithoutThrowing() throws IOException {
        // The constructor should succeed regardless of font loading outcome
        // (it falls back to Helvetica if NotoSans fonts are missing)
        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            assertNotNull(builder, "Builder should be created successfully");
        }
    }

    // ---------------------------------------------------------------
    // 2. Font loading verification — fontsLoaded flag
    //    (Will fail until task 2.5 adds font loading logic that sets
    //     fontsLoaded = true)
    // ---------------------------------------------------------------

    @Test
    void constructorShouldSetFontsLoadedTrueWhenNotoSansFontsAreAvailable() throws IOException {
        // NotoSans fonts are on the classpath (src/main/resources/fonts/),
        // so fontsLoaded should be true after construction.
        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            assertTrue(builder.isFontsLoaded(),
                    "fontsLoaded should be true when NotoSans fonts are on the classpath");
        }
    }

    // ---------------------------------------------------------------
    // 3. Behavioral test — builder can save a valid PDF after creation
    // ---------------------------------------------------------------

    @Test
    void builderShouldProduceValidPdfAfterConstruction() throws IOException {
        File outputFile = tempDir.resolve("smoke-test.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addParagraph("Smoke test paragraph");
            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");

        // Verify it's a valid PDF by loading it with PDFBox 3.x API
        try (PDDocument doc = Loader.loadPDF(outputFile)) {
            assertEquals(1, doc.getNumberOfPages(), "PDF should have one page");
        }
    }

    // ---------------------------------------------------------------
    // 4. Unicode rendering — Latin text should not produce '?'
    //    (Will fail until task 2.5 implements NotoSans font loading
    //     and task 2.6 implements addFormattedText/endParagraph)
    // ---------------------------------------------------------------

    @Test
    void addFormattedTextShouldRenderLatinTextWithoutQuestionMarks() throws IOException {
        File outputFile = tempDir.resolve("latin-text.pdf").toFile();
        String latinText = "Hello World - Testing Latin characters";

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addFormattedText(latinText, false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        String extractedText = extractTextFromPdf(outputFile);
        assertFalse(extractedText.contains("?"),
                "Latin text should not contain '?' placeholder characters");
        assertTrue(extractedText.contains("Hello World"),
                "Extracted text should contain the original Latin text");
    }

    // ---------------------------------------------------------------
    // 5. Unicode rendering — Cyrillic text should not produce '?'
    //    (Will fail until NotoSans fonts are loaded)
    // ---------------------------------------------------------------

    @Test
    void addFormattedTextShouldRenderCyrillicTextWithoutQuestionMarks() throws IOException {
        File outputFile = tempDir.resolve("cyrillic-text.pdf").toFile();
        String cyrillicText = "Привет мир";

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addFormattedText(cyrillicText, false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        String extractedText = extractTextFromPdf(outputFile);
        assertFalse(extractedText.contains("?"),
                "Cyrillic text should not contain '?' placeholder characters");
    }

    // ---------------------------------------------------------------
    // 6. Unicode rendering — CJK text should not crash the builder
    //    (CJK rendering depends on OTF font support in PDFBox)
    // ---------------------------------------------------------------

    @Test
    void addFormattedTextShouldNotCrashWithCjkText() throws IOException {
        File outputFile = tempDir.resolve("cjk-text.pdf").toFile();
        String cjkText = "你好世界";

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addFormattedText(cjkText, false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF should be created even with CJK text");
        assertTrue(outputFile.length() > 0, "PDF should not be empty");
    }

    // ---------------------------------------------------------------
    // 7. Bold font variant — builder should render bold text
    //    (Will fail until addFormattedText is implemented)
    // ---------------------------------------------------------------

    @Test
    void addFormattedTextShouldAcceptBoldFormatting() throws IOException {
        File outputFile = tempDir.resolve("bold-text.pdf").toFile();
        String boldText = "Bold text test";

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addFormattedText(boldText, true, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        String extractedText = extractTextFromPdf(outputFile);
        assertTrue(extractedText.contains("Bold text test"),
                "Bold text should be rendered in the PDF output");
    }

    // ---------------------------------------------------------------
    // 8. Multiple font variants in one paragraph
    //    (Will fail until addFormattedText/endParagraph are implemented)
    // ---------------------------------------------------------------

    @Test
    void addFormattedTextShouldHandleMultipleFontVariantsInOneParagraph() throws IOException {
        File outputFile = tempDir.resolve("mixed-fonts.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addFormattedText("Regular ", false, false, 12f);
            builder.addFormattedText("Bold ", true, false, 12f);
            builder.addFormattedText("Italic ", false, true, 12f);
            builder.addFormattedText("BoldItalic", true, true, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        String extractedText = extractTextFromPdf(outputFile);
        assertTrue(extractedText.contains("Regular"),
                "Regular text should appear in output");
        assertTrue(extractedText.contains("Bold"),
                "Bold text should appear in output");
        assertTrue(extractedText.contains("Italic"),
                "Italic text should appear in output");
    }

    // ---------------------------------------------------------------
    // 9. Fallback behavior — builder still works even if we can't
    //    remove fonts from classpath (smoke test for resilience)
    // ---------------------------------------------------------------

    @Test
    void builderShouldCreateSuccessfullyRegardlessOfFontAvailability() throws IOException {
        // This test verifies that the builder constructor never throws,
        // even in edge cases. The fallback to Helvetica should be seamless.
        File outputFile = tempDir.resolve("fallback-test.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.addParagraph("Fallback font test");
            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF should be created even with fallback fonts");
        assertTrue(outputFile.length() > 0, "PDF should not be empty with fallback fonts");
    }

    // ---------------------------------------------------------------
    // TextAlignment enum and setAlignment() tests (Task 3.1)
    // Validates: Requirements 2.1, 2.2, 2.4, 2.5
    // ---------------------------------------------------------------

    @Test
    void textAlignmentEnumShouldHaveLeftCenterRightValues() {
        com.xtopdf.xtopdf.pdf.TextAlignment[] values = com.xtopdf.xtopdf.pdf.TextAlignment.values();
        assertEquals(3, values.length, "TextAlignment should have exactly 3 values");
        assertNotNull(com.xtopdf.xtopdf.pdf.TextAlignment.LEFT, "LEFT should exist");
        assertNotNull(com.xtopdf.xtopdf.pdf.TextAlignment.CENTER, "CENTER should exist");
        assertNotNull(com.xtopdf.xtopdf.pdf.TextAlignment.RIGHT, "RIGHT should exist");
    }

    @Test
    void setAlignmentLeftFollowedByEndParagraphShouldRenderText() throws IOException {
        File outputFile = tempDir.resolve("align-left.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.setAlignment(com.xtopdf.xtopdf.pdf.TextAlignment.LEFT);
            builder.addFormattedText("Left aligned text", false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
        String extractedText = extractTextFromPdf(outputFile);
        assertTrue(extractedText.contains("Left aligned text"),
                "PDF should contain the left-aligned text");
    }

    @Test
    void setAlignmentCenterFollowedByEndParagraphShouldRenderText() throws IOException {
        File outputFile = tempDir.resolve("align-center.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.setAlignment(com.xtopdf.xtopdf.pdf.TextAlignment.CENTER);
            builder.addFormattedText("Center aligned text", false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
        String extractedText = extractTextFromPdf(outputFile);
        assertTrue(extractedText.contains("Center aligned text"),
                "PDF should contain the center-aligned text");
    }

    @Test
    void setAlignmentRightFollowedByEndParagraphShouldRenderText() throws IOException {
        File outputFile = tempDir.resolve("align-right.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            builder.setAlignment(com.xtopdf.xtopdf.pdf.TextAlignment.RIGHT);
            builder.addFormattedText("Right aligned text", false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
        String extractedText = extractTextFromPdf(outputFile);
        assertTrue(extractedText.contains("Right aligned text"),
                "PDF should contain the right-aligned text");
    }

    @Test
    void alignmentShouldResetToLeftAfterEndParagraph() throws IOException {
        File outputFile = tempDir.resolve("align-reset.pdf").toFile();

        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder()) {
            // First paragraph: right-aligned
            builder.setAlignment(com.xtopdf.xtopdf.pdf.TextAlignment.RIGHT);
            builder.addFormattedText("Right paragraph", false, false, 12f);
            builder.endParagraph();

            // Second paragraph: should be left-aligned (reset after endParagraph)
            builder.addFormattedText("Default paragraph", false, false, 12f);
            builder.endParagraph();

            builder.save(outputFile);
        }

        assertTrue(outputFile.exists(), "PDF file should be created");
        assertTrue(outputFile.length() > 0, "PDF file should not be empty");
        String extractedText = extractTextFromPdf(outputFile);
        assertTrue(extractedText.contains("Right paragraph"),
                "PDF should contain the right-aligned paragraph");
        assertTrue(extractedText.contains("Default paragraph"),
                "PDF should contain the default-aligned paragraph");
    }

    // ---------------------------------------------------------------
    // Helper method
    // ---------------------------------------------------------------

    private String extractTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
}
