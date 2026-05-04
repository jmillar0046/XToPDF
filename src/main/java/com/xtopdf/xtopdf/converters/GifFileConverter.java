package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.GifToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class GifFileConverter implements FileConverter {
    private final GifToPdfService gifToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".gif");
    }

    @Override
    public void convertToPDF(MultipartFile gifFile, String outputFile) throws FileConversionException {
        if (gifFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            gifToPdfService.convertGifToPdf(gifFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting GIF to PDF: " + e.getMessage(), e);
        }
    }
}