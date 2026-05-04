package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.PngToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            pngToPdfService.convertPngToPdf(pngFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting PNG to PDF: " + e.getMessage(), e);
        }
    }
}