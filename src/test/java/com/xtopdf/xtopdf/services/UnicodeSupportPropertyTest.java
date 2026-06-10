package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.services.conversion.document.TxtToPdfService;
import net.jqwik.api.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Property-based tests for Unicode support in TxtFileConverter.
 * Validates that the TxtToPdfService can handle arbitrary Unicode strings
 * from various character ranges without crashing and produces valid PDFs.
 *
 * <p><b>Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6</b></p>
 *
 * Note: We do NOT assert the text appears in the PDF (font-dependent).
 * We only verify no crash and valid PDF output.
 */
class UnicodeSupportPropertyTest {

    @TempDir
    File tempDir;

    private final PdfBackendProvider pdfBackend;
    private final TxtToPdfService txtToPdfService;

    UnicodeSupportPropertyTest() {
        // Use a simple PdfBackendProvider that creates PdfBoxDocumentBuilder using the no-arg constructor.
        // The no-arg constructor loads NotoSans from classpath, falling back to Helvetica if not found.
        this.pdfBackend = new PdfBackendProvider() {
            @Override
            public com.xtopdf.xtopdf.pdf.PdfDocumentBuilder createBuilder() throws java.io.IOException {
                return new com.xtopdf.xtopdf.pdf.impl.PdfBoxDocumentBuilder();
            }

            @Override
            public String getBackendName() {
                return "pdfbox-test";
            }
        };
        this.txtToPdfService = new TxtToPdfService(pdfBackend);
    }

    /**
     * Property 12: Unicode Font Usage
     * For any Unicode string from Latin, CJK, Cyrillic, Arabic, Hebrew, and Emoji ranges,
     * the TxtToPdfService should produce a valid PDF without throwing.
     */
    @Property(tries = 25)
    @Tag("Feature: unicode-support, Property 12: Unicode Font Usage")
    void latinCharactersProduceValidPdf(@ForAll("latinStrings") String text) throws Exception {
        assertValidPdfProduced(text);
    }

    /**
     * Property 13: International Character Rendering
     * CJK characters should not crash the converter.
     */
    @Property(tries = 25)
    @Tag("Feature: unicode-support, Property 13: International Character Rendering")
    void cjkCharactersProduceValidPdf(@ForAll("cjkStrings") String text) throws Exception {
        assertValidPdfProduced(text);
    }

    /**
     * Property 14: Font Fallback Mechanism
     * Cyrillic and Arabic characters should not crash the converter.
     */
    @Property(tries = 25)
    @Tag("Feature: unicode-support, Property 14: Font Fallback Mechanism")
    void cyrillicAndArabicCharactersProduceValidPdf(@ForAll("cyrillicArabicStrings") String text) throws Exception {
        assertValidPdfProduced(text);
    }

    /**
     * Property 15: Font Embedding
     * Mixed Unicode strings (combining multiple scripts) should produce valid PDFs.
     */
    @Property(tries = 25)
    @Tag("Feature: unicode-support, Property 15: Font Embedding")
    void mixedUnicodeProducesValidPdf(@ForAll("mixedUnicodeStrings") String text) throws Exception {
        assertValidPdfProduced(text);
    }

    // --- Providers ---

    @Provide
    Arbitrary<String> latinStrings() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('a', 'z')
                .withCharRange('\u00C0', '\u00FF') // Latin Extended (accented chars)
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(200);
    }

    @Provide
    Arbitrary<String> cjkStrings() {
        return Arbitraries.strings()
                .withCharRange('\u4E00', '\u4FFF') // CJK Unified Ideographs (subset)
                .withCharRange('\u3040', '\u309F') // Hiragana
                .withCharRange('\u30A0', '\u30FF') // Katakana
                .ofMinLength(1)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> cyrillicArabicStrings() {
        return Arbitraries.oneOf(
                Arbitraries.strings()
                        .withCharRange('\u0400', '\u04FF') // Cyrillic
                        .ofMinLength(1)
                        .ofMaxLength(100),
                Arbitraries.strings()
                        .withCharRange('\u0590', '\u05FF') // Hebrew
                        .ofMinLength(1)
                        .ofMaxLength(100),
                Arbitraries.strings()
                        .withCharRange('\u0600', '\u06FF') // Arabic
                        .ofMinLength(1)
                        .ofMaxLength(100)
        );
    }

    @Provide
    Arbitrary<String> mixedUnicodeStrings() {
        // Combine multiple scripts in a single string
        Arbitrary<String> latin = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(30);

        Arbitrary<String> cyrillic = Arbitraries.strings()
                .withCharRange('\u0410', '\u044F') // Cyrillic uppercase+lowercase
                .ofMinLength(1)
                .ofMaxLength(30);

        Arbitrary<String> cjk = Arbitraries.strings()
                .withCharRange('\u4E00', '\u4FFF')
                .ofMinLength(1)
                .ofMaxLength(20);

        Arbitrary<String> emoji = Arbitraries.of(
                "\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02", "\uD83D\uDE03",
                "\uD83D\uDE04", "\uD83D\uDE05", "\uD83C\uDF1F", "\uD83C\uDF08",
                "\u2764", "\u2B50", "\u2705", "\u274C"
        );

        return Combinators.combine(latin, cyrillic, cjk, emoji)
                .as((l, c, k, e) -> l + " " + c + " " + k + " " + e);
    }

    // --- Helper ---

    private void assertValidPdfProduced(String text) throws Exception {
        File outputFile = new File(tempDir, "output_" + System.nanoTime() + ".pdf");

        byte[] content = text.getBytes(StandardCharsets.UTF_8);
        var mockFile = new MockMultipartFile(
                "file", "unicode_test.txt", "text/plain", content);

        // Should not throw
        assertThatNoException().isThrownBy(() ->
                txtToPdfService.convertTxtToPdf(mockFile, outputFile));

        // Output file should exist and be a valid PDF
        assertThat(outputFile).exists();
        assertThat(outputFile.length()).isGreaterThan(0);

        // Verify it's a parseable PDF
        try (PDDocument doc = Loader.loadPDF(outputFile)) {
            assertThat(doc.getNumberOfPages()).isGreaterThanOrEqualTo(1);
        }
    }
}
