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
        var pdfFile = new File(outputFile);
        try {
            svgToPdfService.convertSvgToPdf(svgFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting SVG to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}