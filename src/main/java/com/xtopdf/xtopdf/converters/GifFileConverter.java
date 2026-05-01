package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.GifToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            gifToPdfService.convertGifToPdf(gifFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting GIF to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}