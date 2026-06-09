package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.data.HtmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

/**
 * Converts HTML files to PDF format using Flying Saucer (XHTML renderer).
 *
 * <p><b>Rendering Approach:</b> Uses JSoup to parse and clean HTML into well-formed XHTML,
 * then renders via Flying Saucer with OpenPDF backend for full CSS support.</p>
 *
 * <p><b>Supported Features:</b></p>
 * <ul>
 *   <li>Inline and internal CSS styles</li>
 *   <li>Tables, lists, headings, paragraphs</li>
 *   <li>Colors, fonts, margins, padding, borders</li>
 *   <li>Data URI embedded images</li>
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
