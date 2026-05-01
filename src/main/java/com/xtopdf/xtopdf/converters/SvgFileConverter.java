package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.SvgToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@AllArgsConstructor
@Component
public class SvgFileConverter implements FileConverter {
    private final SvgToPdfService svgToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".svg");
    }

    @Override
    public void convertToPDF(MultipartFile svgFile, String outputFile) throws FileConversionException {
        if (svgFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        try {
            svgToPdfService.convertSvgToPdf(svgFile, new File(outputFile));
        } catch (IOException e) {
            throw new RuntimeException("Error converting SVG to PDF: " + e.getMessage(), e);
        }
    }
}