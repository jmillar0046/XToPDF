package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.conversion.image.SvgToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

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
