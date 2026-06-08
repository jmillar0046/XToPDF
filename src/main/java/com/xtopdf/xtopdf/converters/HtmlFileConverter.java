package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.data.HtmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

/**
 * Converts HTML files to PDF format.
 *
 * <p><b>Rendering Approach:</b> Uses JSoup for HTML parsing and PDFBox for PDF generation.
 * The HTML is parsed into a DOM tree, then text content is extracted and rendered as
 * a plain-text PDF document with basic formatting.</p>
 *
 * <p><b>Known Limitations:</b></p>
 * <ul>
 *   <li>CSS styling is not fully rendered — only basic structural elements are preserved</li>
 *   <li>JavaScript is not executed — dynamic content will not be captured</li>
 *   <li>External resources (images, fonts, stylesheets) are not fetched or embedded</li>
 *   <li>Complex layouts (tables, flexbox, grid) are simplified to linear text flow</li>
 *   <li>For pixel-perfect HTML-to-PDF rendering, use a headless browser-based solution</li>
 * </ul>
 */
@AllArgsConstructor
@Component
public class HtmlFileConverter extends AbstractFileConverter {
    private final HtmlToPdfService htmlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".html");
    }

    @Override
    protected String getFormatName() {
        return "HTML";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        htmlToPdfService.convertHtmlToPdf(inputFile, new File(outputFile));
    }
}
