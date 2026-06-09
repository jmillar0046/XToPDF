package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.SvgToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

/**
 * Converts SVG (Scalable Vector Graphics) files to PDF format using Apache Batik.
 *
 * <p><b>Rendering Approach:</b> Uses Batik's transcoder to render SVG to a high-resolution
 * PNG, then embeds the rendered image into a PDF via PDFBox. This supports the full
 * SVG specification including gradients, paths, text, transforms, and filters.</p>
 *
 * <p><b>Supported Features:</b></p>
 * <ul>
 *   <li>All SVG shape elements (path, rect, circle, ellipse, line, polyline, polygon)</li>
 *   <li>Gradients (linear and radial)</li>
 *   <li>Text rendering</li>
 *   <li>CSS styling (inline and internal)</li>
 *   <li>Transforms and filters</li>
 *   <li>Animations stripped for static first-frame output</li>
 * </ul>
 */
@AllArgsConstructor
@Component
public class SvgFileConverter extends AbstractFileConverter {
    private final SvgToPdfService svgToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".svg");
    }

    @Override
    protected String getFormatName() {
        return "SVG";
    }

    @Override
    protected void doConvert(MultipartFile inputFile, String outputFile) throws Exception {
        svgToPdfService.convertSvgToPdf(inputFile, new File(outputFile));
    }
}
