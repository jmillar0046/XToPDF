package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.SvgToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

/**
 * Converts SVG (Scalable Vector Graphics) files to PDF format.
 *
 * <p><b>Rendering Approach:</b> Parses SVG XML structure and renders vector elements
 * (paths, shapes, text) to PDF using PDFBox graphics primitives. The conversion
 * preserves vector fidelity where possible.</p>
 *
 * <p><b>Known Limitations:</b></p>
 * <ul>
 *   <li>CSS styling within SVG is partially supported — inline styles work best</li>
 *   <li>External references (xlink:href to external files) are not resolved</li>
 *   <li>SVG filters (blur, drop-shadow) are not rendered</li>
 *   <li>Embedded fonts may not be available — falls back to system fonts</li>
 *   <li>Complex SVG features (animations, scripting, foreignObject) are ignored</li>
 *   <li>Gradient and pattern fills may be simplified</li>
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
