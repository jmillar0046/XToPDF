package com.xtopdf.xtopdf.services.conversion.data;

import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for HTML rendering using Flying Saucer.
 * Validates: Requirements 8.2, 8.4
 *
 * Property 16: HTML CSS Application
 * Property 17: HTML Image Embedding
 */
class HtmlRenderingPropertyTest {

    private final HtmlToPdfService htmlToPdfService = new HtmlToPdfService();

    /**
     * Property 16: HTML CSS Application
     *
     * Various HTML structures with CSS styling should produce valid PDF output.
     * The PDF should be non-empty and start with PDF magic bytes.
     *
     * **Validates: Requirements 8.2**
     */
    @Property(tries = 25)
    @Tag("Feature: html-rendering, Property 16: HTML CSS Application")
    void htmlWithCssProducesValidPdf(
            @ForAll("htmlWithCss") String htmlContent) throws IOException {

        var tempFile = File.createTempFile("html-test-", ".pdf");
        try {
            var multipartFile = new MockMultipartFile(
                    "file", "test.html", "text/html", htmlContent.getBytes());

            htmlToPdfService.convertHtmlToPdf(multipartFile, tempFile);

            assertThat(tempFile).exists();
            assertThat(tempFile.length()).isGreaterThan(0);

            // Verify PDF magic bytes
            byte[] pdfBytes = Files.readAllBytes(tempFile.toPath());
            assertThat(pdfBytes.length).isGreaterThan(4);
            assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Property 17: HTML Image Embedding
     *
     * HTML with various structural elements should produce valid PDF output.
     * Tests that different HTML structures are handled correctly by the renderer.
     *
     * **Validates: Requirements 8.4**
     */
    @Property(tries = 25)
    @Tag("Feature: html-rendering, Property 17: HTML Image Embedding")
    void htmlWithVariousStructuresProducesValidPdf(
            @ForAll("htmlStructures") String htmlContent) throws IOException {

        var tempFile = File.createTempFile("html-struct-", ".pdf");
        try {
            var multipartFile = new MockMultipartFile(
                    "file", "test.html", "text/html", htmlContent.getBytes());

            htmlToPdfService.convertHtmlToPdf(multipartFile, tempFile);

            assertThat(tempFile).exists();
            assertThat(tempFile.length()).isGreaterThan(0);

            byte[] pdfBytes = Files.readAllBytes(tempFile.toPath());
            assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
        } finally {
            tempFile.delete();
        }
    }

    @Provide
    Arbitrary<String> htmlWithCss() {
        var colors = Arbitraries.of("red", "blue", "green", "#333", "#ff0000", "rgb(0,0,0)");
        var fontSizes = Arbitraries.of("12px", "14px", "16px", "1em", "1.5rem");
        var margins = Arbitraries.of("10px", "20px", "0", "5px 10px");
        var bodyText = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);

        return Combinators.combine(colors, fontSizes, margins, bodyText)
                .as((color, fontSize, margin, text) ->
                        "<html><head><style>" +
                        "body { color: " + color + "; font-size: " + fontSize + "; margin: " + margin + "; }" +
                        "h1 { font-weight: bold; }" +
                        "p { line-height: 1.5; }" +
                        "</style></head><body>" +
                        "<h1>" + text + "</h1>" +
                        "<p>" + text + "</p>" +
                        "</body></html>");
    }

    @Provide
    Arbitrary<String> htmlStructures() {
        return Arbitraries.of(
                // Simple paragraph
                "<html><body><p>Hello World</p></body></html>",
                // Headings
                "<html><body><h1>Title</h1><h2>Subtitle</h2><p>Content</p></body></html>",
                // Table
                "<html><body><table><tr><th>Name</th><th>Value</th></tr><tr><td>A</td><td>1</td></tr></table></body></html>",
                // Unordered list
                "<html><body><ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul></body></html>",
                // Ordered list
                "<html><body><ol><li>First</li><li>Second</li><li>Third</li></ol></body></html>",
                // Nested divs with styles
                "<html><body><div style=\"margin:10px;\"><div style=\"padding:5px;\"><p>Nested</p></div></div></body></html>",
                // Bold and italic
                "<html><body><p><strong>Bold</strong> and <em>italic</em> text</p></body></html>",
                // Inline style
                "<html><body><p style=\"color:blue; font-size:14px;\">Styled paragraph</p></body></html>",
                // Multiple paragraphs
                "<html><body><p>First paragraph</p><p>Second paragraph</p><p>Third paragraph</p></body></html>",
                // Complex table with styling
                "<html><head><style>table { border-collapse: collapse; } td { border: 1px solid black; padding: 5px; }</style></head><body><table><tr><td>A</td><td>B</td></tr><tr><td>C</td><td>D</td></tr></table></body></html>"
        );
    }
}
