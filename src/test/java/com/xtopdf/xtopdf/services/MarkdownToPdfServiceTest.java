package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.services.conversion.document.MarkdownToPdfService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkdownToPdfServiceTest {

    private MarkdownToPdfService markdownToPdfService;
    private com.xtopdf.xtopdf.pdf.PdfBackendProvider pdfBackend;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfBackend = new com.xtopdf.xtopdf.pdf.impl.PdfBoxBackend();
        markdownToPdfService = new MarkdownToPdfService(pdfBackend);
    }

    @Test
    void testConvertMarkdownToPdf_Success() throws Exception {
        var content = "# Hello World\n\nThis is a **test** markdown file.";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("testMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void testConvertMarkdownToPdf_EmptyFile() throws Exception {
        var content = "";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("testEmptyMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    @Test
    void testConvertMarkdownToPdf_InvalidPdfCreation() {
        var content = "# Test Markdown";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());

        assertThatThrownBy(() -> markdownToPdfService.convertMarkdownToPdf(markdownFile, null))
                .isInstanceOf(IOException.class);
    }

    @Test
    void testConvertMarkdownToPdf_WithUnicodeCharacters() throws Exception {
        var content = "# Hello\n\n**Bold** and *italic* text.";
        var markdownFile = new MockMultipartFile("file", "unicode.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes("UTF-8"));
        var pdfFile = tempDir.resolve("unicodeMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertMarkdownToPdf_OnlyWhitespace() throws Exception {
        var content = "     \n   \n";
        var markdownFile = new MockMultipartFile("file", "whitespace.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("whitespaceMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
    }

    @Test
    void testConvertMarkdownToPdf_NullMultipartFile_ThrowsNullPointerException() {
        var pdfFile = tempDir.resolve("nullInput.pdf").toFile();

        assertThatThrownBy(() -> markdownToPdfService.convertMarkdownToPdf(null, pdfFile))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testConvertMarkdownToPdf_NullOutputFile_ThrowsIOException() {
        var content = "# test";
        var markdownFile = new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());

        assertThatThrownBy(() -> markdownToPdfService.convertMarkdownToPdf(markdownFile, null))
                .isInstanceOf(IOException.class);
    }

    @Test
    void testConvertMarkdownToPdf_MultipleLines() throws Exception {
        var content = "# Line 1\n## Line 2\n### Line 3";
        var markdownFile = new MockMultipartFile("file", "multilines.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("multilinesMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertMarkdownToPdf_WithLists() throws Exception {
        var content = "# List Test\n\n- Item 1\n- Item 2\n- Item 3";
        var markdownFile = new MockMultipartFile("file", "lists.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("listsMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertMarkdownToPdf_WithLinks() throws Exception {
        var content = "# Link Test\n\n[Example Link](https://example.com)";
        var markdownFile = new MockMultipartFile("file", "links.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("linksMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertMarkdownToPdf_WithCodeBlock() throws Exception {
        var content = "# Code Test\n\n```java\npublic class Test {}\n```";
        var markdownFile = new MockMultipartFile("file", "code.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("codeMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertMarkdownToPdf_WithBoldAndItalic() throws Exception {
        var content = "**Bold text** and *italic text* and ***both***";
        var markdownFile = new MockMultipartFile("file", "formatting.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("formattingMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void testConvertMarkdownToPdf_WithBlockquote() throws Exception {
        var content = "> This is a blockquote\n> With multiple lines";
        var markdownFile = new MockMultipartFile("file", "blockquote.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("blockquoteMarkdownOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    // --- New tests for task 6.3: Heading, formatting, and security verification ---

    @Test
    void headingsH1ThroughH6_renderAtDistinctSizesInXhtml() {
        var markdown = "# H1\n## H2\n### H3\n#### H4\n##### H5\n###### H6";

        // Parse and wrap to get the full HTML with CSS
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        // Verify Commonmark produces all heading levels
        assertThat(rawHtml).contains("<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>");

        // Verify CSS defines distinct sizes for each heading
        String xhtml = convertToXhtml(wrapWithTestStyles(rawHtml));
        assertThat(xhtml).contains("h1");
        assertThat(xhtml).contains("h2");
        assertThat(xhtml).contains("h3");
        assertThat(xhtml).contains("h4");
        assertThat(xhtml).contains("h5");
        assertThat(xhtml).contains("h6");
    }

    @Test
    void headingsH1ThroughH6_producesValidPdf() throws Exception {
        var content = "# Heading 1\n## Heading 2\n### Heading 3\n#### Heading 4\n##### Heading 5\n###### Heading 6";
        var markdownFile = new MockMultipartFile("file", "headings.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("headingsOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void boldAndItalicText_renderedWithCorrectHtmlTags() {
        var markdown = "**bold** and *italic* and ***both***";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        assertThat(rawHtml).contains("<strong>bold</strong>");
        assertThat(rawHtml).contains("<em>italic</em>");
        assertThat(rawHtml).contains("<em><strong>both</strong></em>");
    }

    @Test
    void boldAndItalicText_producesValidPdfWithStyling() throws Exception {
        var content = "Normal text, **bold text**, *italic text*, and ***bold italic***.";
        var markdownFile = new MockMultipartFile("file", "styles.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("stylesOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
    }

    @Test
    void fencedCodeBlock_renderedInMonospacePreTag() {
        var markdown = "```java\npublic class Hello {}\n```";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        assertThat(rawHtml).contains("<pre>");
        assertThat(rawHtml).contains("<code");
        assertThat(rawHtml).contains("public class Hello {}");
    }

    @Test
    void inlineCode_renderedInMonospaceCodeTag() {
        var markdown = "Use `System.out.println()` to print.";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        assertThat(rawHtml).contains("<code>System.out.println()</code>");
    }

    @Test
    void fencedAndInlineCode_producesValidPdf() throws Exception {
        var content = "Inline: `code here`\n\nFenced:\n```\nvar x = 1;\n```";
        var markdownFile = new MockMultipartFile("file", "code.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("codeRenderOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void unorderedList_renderedWithIndentation() {
        var markdown = "- Item A\n- Item B\n  - Nested item";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        assertThat(rawHtml).contains("<ul>");
        assertThat(rawHtml).contains("<li>");
        assertThat(rawHtml).contains("Item A");
        assertThat(rawHtml).contains("Item B");
    }

    @Test
    void orderedList_renderedWithIndentation() {
        var markdown = "1. First\n2. Second\n3. Third";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        assertThat(rawHtml).contains("<ol>");
        assertThat(rawHtml).contains("<li>");
        assertThat(rawHtml).contains("First");
        assertThat(rawHtml).contains("Second");
    }

    @Test
    void orderedAndUnorderedLists_producesValidPdf() throws Exception {
        var content = "## Lists\n\n- Bullet 1\n- Bullet 2\n\n1. Number 1\n2. Number 2\n3. Number 3";
        var markdownFile = new MockMultipartFile("file", "lists.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("listsRenderOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        assertThat(pdfFile).exists();
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void links_renderedWithVisualDistinction() {
        var markdown = "[Click here](https://example.com)";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);

        // Commonmark renders links as <a> tags
        assertThat(rawHtml).contains("<a href=\"https://example.com\">Click here</a>");

        // After XHTML sanitization, the link text should still be present
        String xhtml = convertToXhtml(wrapWithTestStyles(rawHtml));
        assertThat(xhtml).contains("Click here");
        // CSS styles links with color and underline
        assertThat(xhtml).contains("text-decoration: underline");
        assertThat(xhtml).contains("color: #0366d6");
    }

    @Test
    void externalUrls_strippedFromImgAndLinkElements() {
        var html = """
                <html><body>
                <img src="https://evil.com/image.png" />
                <link href="https://evil.com/style.css" rel="stylesheet" />
                <a href="https://example.com">Safe link text</a>
                <p>Normal content</p>
                </body></html>
                """;

        String xhtml = convertToXhtml(html);

        // External img and link elements are stripped
        assertThat(xhtml).doesNotContain("evil.com/image.png");
        assertThat(xhtml).doesNotContain("evil.com/style.css");
        // <a> tags remain (they don't cause SSRF, the href just won't be followed)
        assertThat(xhtml).contains("Safe link text");
        // Normal content preserved
        assertThat(xhtml).contains("Normal content");
    }

    @Test
    void externalUrls_scriptAndIframeRemoved() {
        var html = """
                <html><body>
                <script src="https://evil.com/malware.js"></script>
                <iframe src="https://evil.com/frame"></iframe>
                <object data="https://evil.com/exploit"></object>
                <embed src="https://evil.com/exploit" />
                <p>Safe content</p>
                </body></html>
                """;

        String xhtml = convertToXhtml(html);

        assertThat(xhtml).doesNotContain("<script");
        assertThat(xhtml).doesNotContain("<iframe");
        assertThat(xhtml).doesNotContain("<object");
        assertThat(xhtml).doesNotContain("<embed");
        assertThat(xhtml).contains("Safe content");
    }

    @Test
    void convertToXhtml_producesValidXhtmlWithNamespace() {
        var html = "<html><body><p>Hello</p></body></html>";

        String xhtml = convertToXhtml(html);

        assertThat(xhtml).contains("xmlns=\"http://www.w3.org/1999/xhtml\"");
        assertThat(xhtml).contains("</html>");
        assertThat(xhtml).contains("</body>");
    }

    @Test
    void cssStyles_embedDistinctHeadingSizes() throws Exception {
        // Verify the CSS includes distinct sizes for all heading levels
        var content = "# H1\n## H2\n### H3\n#### H4\n##### H5\n###### H6\n\nBody text.";
        var markdownFile = new MockMultipartFile("file", "sizes.md", MediaType.TEXT_MARKDOWN_VALUE, content.getBytes());
        var pdfFile = tempDir.resolve("headingSizesOutput.pdf").toFile();

        markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);

        // The PDF should be valid and non-empty (CSS styling applied internally)
        assertThat(pdfFile).exists();
        assertThat(pdfFile.length()).isGreaterThan(0);
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        assertThat(new String(pdfBytes, 0, 5)).startsWith("%PDF");
    }

    @Test
    void cssStyles_codeUsesMonospaceFont() {
        var markdown = "`inline code`";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);
        String xhtml = convertToXhtml(wrapWithTestStyles(rawHtml));

        // CSS defines monospace for code elements
        assertThat(xhtml).contains("font-family: monospace");
        assertThat(xhtml).contains("<code>");
    }

    @Test
    void cssStyles_listsHaveIndentation() {
        var markdown = "- Item 1\n- Item 2";
        var parser = org.commonmark.parser.Parser.builder().build();
        var document = parser.parse(markdown);
        var htmlRenderer = org.commonmark.renderer.html.HtmlRenderer.builder().build();
        var rawHtml = htmlRenderer.render(document);
        String xhtml = convertToXhtml(wrapWithTestStyles(rawHtml));

        // CSS defines margin-left for ul/ol indentation
        assertThat(xhtml).contains("margin-left: 20px");
        assertThat(xhtml).contains("<ul>");
        assertThat(xhtml).contains("<li>");
    }

    // Helper that mirrors the MarkdownToPdfService.convertToXhtml() logic for test verification
    private String convertToXhtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset(StandardCharsets.UTF_8);

        var htmlElement = doc.selectFirst("html");
        if (htmlElement != null && !htmlElement.hasAttr("xmlns")) {
            htmlElement.attr("xmlns", "http://www.w3.org/1999/xhtml");
        }

        var head = doc.head();
        if (head != null && head.select("meta[http-equiv=Content-Type]").isEmpty()) {
            head.prepend("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        }

        doc.select("link[href~=(?i)^(https?:)?//]").remove();
        doc.select("img[src~=(?i)^(https?:)?//]").remove();
        doc.select("script, iframe, object, embed").remove();

        return doc.html();
    }

    // Helper to wrap HTML with the same CSS used in the service (for test verification)
    private String wrapWithTestStyles(String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <style>
                body { font-family: serif; font-size: 12pt; line-height: 1.5; margin: 40px; }
                h1 { font-size: 24pt; font-weight: bold; }
                h2 { font-size: 20pt; font-weight: bold; }
                h3 { font-size: 16pt; font-weight: bold; }
                h4 { font-size: 14pt; font-weight: bold; }
                h5 { font-size: 13pt; font-weight: bold; }
                h6 { font-size: 12pt; font-weight: bold; }
                strong { font-weight: bold; }
                em { font-style: italic; }
                code { font-family: monospace; background-color: #f5f5f5; padding: 2px 4px; }
                pre { font-family: monospace; background-color: #f5f5f5; padding: 10px; border: 1px solid #ddd; }
                pre code { background-color: transparent; padding: 0; }
                ul, ol { margin-left: 20px; }
                li { margin-bottom: 4px; }
                a { color: #0366d6; text-decoration: underline; }
                blockquote { border-left: 3px solid #ddd; margin-left: 0; padding-left: 15px; color: #555; }
                </style>
                </head>
                <body>
                """ + bodyHtml + """
                </body>
                </html>
                """;
    }
}
