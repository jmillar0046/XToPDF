package com.xtopdf.xtopdf.services.conversion.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for HtmlToPdfService edge cases.
 * Tests malformed HTML, empty HTML, and large HTML handling.
 */
class HtmlToPdfServiceTest {

    private HtmlToPdfService htmlToPdfService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        htmlToPdfService = new HtmlToPdfService();
    }

    @Test
    void convertHtmlToPdf_withValidHtml_producesValidPdf() throws IOException {
        var html = "<html><body><h1>Hello</h1><p>World</p></body></html>";
        var file = new MockMultipartFile("file", "test.html", "text/html", html.getBytes());
        var outputFile = tempDir.resolve("output.pdf").toFile();

        htmlToPdfService.convertHtmlToPdf(file, outputFile);

        assertThat(outputFile).exists();
        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void convertHtmlToPdf_withEmptyHtml_producesValidPdf() throws IOException {
        var html = "";
        var file = new MockMultipartFile("file", "empty.html", "text/html", html.getBytes());
        var outputFile = tempDir.resolve("empty.pdf").toFile();

        htmlToPdfService.convertHtmlToPdf(file, outputFile);

        // Empty HTML still produces a valid (empty) PDF document
        assertThat(outputFile).exists();
        assertThat(outputFile.length()).isGreaterThan(0);
    }

    @Test
    void convertHtmlToPdf_withMalformedHtml_producesValidPdf() throws IOException {
        // Malformed HTML with unclosed tags - JSoup should clean it
        var html = "<html><body><p>Unclosed paragraph<div>Nested wrong</p></div><b>Unclosed bold";
        var file = new MockMultipartFile("file", "malformed.html", "text/html", html.getBytes());
        var outputFile = tempDir.resolve("malformed.pdf").toFile();

        htmlToPdfService.convertHtmlToPdf(file, outputFile);

        assertThat(outputFile).exists();
        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void convertHtmlToPdf_withLargeHtml_producesValidPdf() throws IOException {
        // Generate large HTML with many paragraphs
        var sb = new StringBuilder("<html><body>");
        for (int i = 0; i < 200; i++) {
            sb.append("<p>Paragraph ").append(i)
              .append(" with some content to make it larger.</p>");
        }
        sb.append("</body></html>");

        var file = new MockMultipartFile("file", "large.html", "text/html", sb.toString().getBytes());
        var outputFile = tempDir.resolve("large.pdf").toFile();

        htmlToPdfService.convertHtmlToPdf(file, outputFile);

        assertThat(outputFile).exists();
        assertThat(outputFile.length()).isGreaterThan(0);
        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void convertHtmlToPdf_withCssStyles_producesValidPdf() throws IOException {
        var html = """
                <html>
                <head>
                    <style>
                        body { font-family: sans-serif; margin: 20px; }
                        h1 { color: navy; border-bottom: 2px solid navy; }
                        .highlight { background-color: yellow; padding: 5px; }
                        table { border-collapse: collapse; width: 100%; }
                        td, th { border: 1px solid #ddd; padding: 8px; }
                    </style>
                </head>
                <body>
                    <h1>Styled Document</h1>
                    <p class="highlight">Highlighted text</p>
                    <table>
                        <tr><th>Header 1</th><th>Header 2</th></tr>
                        <tr><td>Cell 1</td><td>Cell 2</td></tr>
                    </table>
                </body>
                </html>
                """;
        var file = new MockMultipartFile("file", "styled.html", "text/html", html.getBytes());
        var outputFile = tempDir.resolve("styled.pdf").toFile();

        htmlToPdfService.convertHtmlToPdf(file, outputFile);

        assertThat(outputFile).exists();
        byte[] pdfBytes = Files.readAllBytes(outputFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void convertToXhtml_cleansMalformedHtml() {
        var malformed = "<html><body><p>Unclosed<div>Nested</p></div>";

        var xhtml = htmlToPdfService.convertToXhtml(malformed);

        // Should produce well-formed XML with xmlns
        assertThat(xhtml).contains("xmlns=\"http://www.w3.org/1999/xhtml\"");
        // Should be parseable (no unclosed tags)
        assertThat(xhtml).contains("</html>");
        assertThat(xhtml).contains("</body>");
    }

    @Test
    void convertToXhtml_addsXmlnsAttribute() {
        var html = "<html><body><p>Test</p></body></html>";

        var xhtml = htmlToPdfService.convertToXhtml(html);

        assertThat(xhtml).contains("xmlns=\"http://www.w3.org/1999/xhtml\"");
    }

    @Test
    void convertToXhtml_preservesExistingXmlns() {
        var html = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><p>Test</p></body></html>";

        var xhtml = htmlToPdfService.convertToXhtml(html);

        // Should not duplicate xmlns
        int count = xhtml.split("xmlns").length - 1;
        assertThat(count).isEqualTo(1);
    }
}
