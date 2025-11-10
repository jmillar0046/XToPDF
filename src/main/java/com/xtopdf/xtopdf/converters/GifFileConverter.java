package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.GifToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class GifFileConverter implements FileConverter {
    private final GifToPdfService gifToPdfService;

    @Override
    public void convertToPDF(MultipartFile gifFile, String outputFile) {
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
