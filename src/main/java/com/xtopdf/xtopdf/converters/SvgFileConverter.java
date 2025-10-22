package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.SvgToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class SvgFileConverter implements FileConverter {
    private final SvgToPdfService svgToPdfService;

    @Override
    public void convertToPDF(MultipartFile svgFile, String outputFile) {
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