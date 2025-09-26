package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.PngToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class PngFileConverter implements FileConverter {
    private final PngToPdfService pngToPdfService;

    @Override
    public void convertToPDF(MultipartFile pngFile, String outputFile) {
        if (pngFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        var pdfFile = new File(outputFile);
        try {
            pngToPdfService.convertPngToPdf(pngFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting PNG to PDF: " + e.getMessage(), e);
        }
    }
}