package com.xtopdf.xtopdf.pdf.impl;

import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for font caching in PdfBoxBackend.
 *
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4
 */
class PdfBoxBackendTest {

    @TempDir
    Path tempDir;

    private PdfBoxBackend backend;

    @BeforeEach
    void setUp() {
        backend = new PdfBoxBackend();
        backend.loadFontBytes();
    }

    // ---------------------------------------------------------------
    // 1. Font bytes loaded during initialization
    // Validates: Requirement 5.1
    // ---------------------------------------------------------------

    @Test
    void loadFontBytesShouldLoadRegularFontBytes() {
        assertThat(backend.getRegularFontBytes())
                .as("Regular font bytes should be loaded from classpath")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void loadFontBytesShouldLoadBoldFontBytes() {
        assertThat(backend.getBoldFontBytes())
                .as("Bold font bytes should be loaded from classpath")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    void loadFontBytesShouldLoadCjkFontBytes() {
        // CJK font may or may not be available depending on classpath;
        // if the OTF is present, bytes should be non-null
        byte[] cjkBytes = backend.getCjkFontBytes();
        // NotoSansCJK-Regular.otf is on the classpath in this project
        assertThat(cjkBytes)
                .as("CJK font bytes should be loaded from classpath")
                .isNotNull()
                .isNotEmpty();
    }

    // ---------------------------------------------------------------
    // 2. createBuilder() returns a builder that produces valid PDFs
    // Validates: Requirement 5.2
    // ---------------------------------------------------------------

    @Test
    void createBuilderShouldReturnBuilderThatProducesValidPdf() throws IOException {
        File outputFile = tempDir.resolve("backend-builder.pdf").toFile();

        try (PdfDocumentBuilder builder = backend.createBuilder()) {
            builder.addFormattedText("Hello from cached fonts", false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        assertThat(outputFile).exists();
        assertThat(outputFile.length()).isGreaterThan(0);

        try (PDDocument doc = Loader.loadPDF(outputFile)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }
    }

    // ---------------------------------------------------------------
    // 3. Multiple createBuilder() calls reuse the same cached byte arrays
    // Validates: Requirement 5.3
    // ---------------------------------------------------------------

    @Test
    void multipleCreateBuilderCallsShouldReuseSameCachedFontByteArrays() throws IOException {
        byte[] regularBefore = backend.getRegularFontBytes();
        byte[] boldBefore = backend.getBoldFontBytes();
        byte[] cjkBefore = backend.getCjkFontBytes();

        // Create multiple builders
        try (PdfDocumentBuilder builder1 = backend.createBuilder();
             PdfDocumentBuilder builder2 = backend.createBuilder();
             PdfDocumentBuilder builder3 = backend.createBuilder()) {
            // Builders created successfully
        }

        // The cached byte arrays should be the exact same object references
        assertThat(backend.getRegularFontBytes())
                .as("Regular font bytes should be the same instance after multiple createBuilder calls")
                .isSameAs(regularBefore);
        assertThat(backend.getBoldFontBytes())
                .as("Bold font bytes should be the same instance after multiple createBuilder calls")
                .isSameAs(boldBefore);
        assertThat(backend.getCjkFontBytes())
                .as("CJK font bytes should be the same instance after multiple createBuilder calls")
                .isSameAs(cjkBefore);
    }

    // ---------------------------------------------------------------
    // 4. Builder falls back to Helvetica when font bytes are null
    // Validates: Requirement 5.4
    // ---------------------------------------------------------------

    @Test
    void builderShouldFallBackToHelveticaWhenFontBytesAreNull() throws IOException {
        File outputFile = tempDir.resolve("null-fonts.pdf").toFile();

        // Create a builder directly with null font bytes to simulate missing fonts
        try (PdfBoxDocumentBuilder builder = new PdfBoxDocumentBuilder(null, null, null)) {
            assertThat(builder.isFontsLoaded())
                    .as("fontsLoaded should be false when all font bytes are null")
                    .isFalse();

            builder.addFormattedText("Helvetica fallback text", false, false, 12f);
            builder.endParagraph();
            builder.save(outputFile);
        }

        assertThat(outputFile).exists();
        assertThat(outputFile.length()).isGreaterThan(0);

        // Verify it's a valid PDF
        try (PDDocument doc = Loader.loadPDF(outputFile)) {
            assertThat(doc.getNumberOfPages()).isEqualTo(1);
        }
    }
}
