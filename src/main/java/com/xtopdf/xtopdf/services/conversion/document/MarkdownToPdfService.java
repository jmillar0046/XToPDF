package com.xtopdf.xtopdf.services.conversion.document;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service to convert Markdown files to PDF.
 * Parses Markdown → HTML (Commonmark) → sanitized XHTML (JSoup) → PDF (Flying Saucer).
 *
 * <p>This follows the proven {@code HtmlToPdfService} pattern for high-quality
 * rendering with proper CSS styling for headings, bold, italic, code, lists, and links.</p>
 */
@Slf4j
@Service
public class MarkdownToPdfService {

    // Retained for API compatibility with MarkdownFileConverter (constructor injection)
    @SuppressWarnings("unused")
    private final PdfBackendProvider pdfBackend;

    public MarkdownToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertMarkdownToPdf(MultipartFile markdownFile, File pdfFile) throws IOException {
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }
        String markdown = new String(markdownFile.getBytes(), StandardCharsets.UTF_8);

        // 1. Parse Markdown to HTML via Commonmark HtmlRenderer
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        String rawHtml = htmlRenderer.render(document);

        // 2. Wrap in full HTML document with embedded CSS
        String fullHtml = wrapWithDefaultStyles(rawHtml);

        // 3. Sanitize to XHTML (strip external URLs, dangerous elements)
        String xhtml = convertToXhtml(fullHtml);

        // 4. Render to PDF via Flying Saucer
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            var renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(os);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from markdown", e);
        }

        log.info("PDF created successfully from Markdown: {}", pdfFile.getName());
    }

    /**
     * Wraps an HTML body fragment in a full HTML document with embedded CSS styles.
     * Provides styling for headings (h1-h6), bold/italic, code blocks, lists, and links.
     */
    private String wrapWithDefaultStyles(String bodyHtml) {
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

    /**
     * Converts raw HTML to well-formed XHTML suitable for Flying Saucer.
     * Strips external resource references (http/https) to prevent SSRF.
     * Removes dangerous elements (script, iframe, object, embed).
     */
    String convertToXhtml(String html) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml)
                .charset(StandardCharsets.UTF_8);

        // Ensure html element has proper namespace for XHTML
        var htmlElement = doc.selectFirst("html");
        if (htmlElement != null && !htmlElement.hasAttr("xmlns")) {
            htmlElement.attr("xmlns", "http://www.w3.org/1999/xhtml");
        }

        // Ensure there's a head element with content-type meta
        var head = doc.head();
        if (head != null && head.select("meta[http-equiv=Content-Type]").isEmpty()) {
            head.prepend("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        }

        // Remove external resource references to prevent SSRF
        doc.select("link[href~=(?i)^(https?:)?//]").remove();
        doc.select("img[src~=(?i)^(https?:)?//]").remove();
        doc.select("script, iframe, object, embed").remove();

        return doc.html();
    }
}
