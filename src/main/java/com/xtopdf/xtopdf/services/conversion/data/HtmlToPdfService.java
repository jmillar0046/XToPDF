package com.xtopdf.xtopdf.services.conversion.data;

import lombok.extern.slf4j.Slf4j;
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
 * Service to convert HTML files to PDF using Flying Saucer (OpenPDF backend).
 *
 * <p>Flying Saucer renders well-formed XHTML with CSS support to PDF.
 * HTML input is first cleaned and converted to XHTML using JSoup, then
 * rendered to PDF via Flying Saucer's ITextRenderer.</p>
 *
 * <p><b>Supported Features:</b></p>
 * <ul>
 *   <li>Inline CSS styles</li>
 *   <li>Internal &lt;style&gt; blocks</li>
 *   <li>Basic CSS layout (margins, padding, borders, colors, fonts)</li>
 *   <li>Tables, lists, headings</li>
 *   <li>Data URI embedded images</li>
 * </ul>
 */
@Service
@Slf4j
public class HtmlToPdfService {

    public void convertHtmlToPdf(MultipartFile htmlFile, File pdfFile) throws IOException {
        var htmlContent = new String(htmlFile.getBytes(), StandardCharsets.UTF_8);
        var xhtml = convertToXhtml(htmlContent);

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            var renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(os);
        } catch (Exception e) {
            throw new IOException("Error converting HTML to PDF: " + e.getMessage(), e);
        }

        log.info("PDF created successfully from HTML: {}", pdfFile.getName());
    }

    /**
     * Converts raw HTML to well-formed XHTML suitable for Flying Saucer.
     * Uses JSoup to parse potentially malformed HTML and output clean XHTML.
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

        return doc.html();
    }
}
