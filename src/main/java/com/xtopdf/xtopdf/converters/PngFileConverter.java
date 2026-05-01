package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.PngToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@AllArgsConstructor
@Component
public class PngFileConverter implements FileConverter {
    private final PngToPdfService pngToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".png");
    }

    @Override
    public void convertToPDF(MultipartFile pngFile, String outputFile) throws FileConversionException {
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