package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.JpegToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class JpegFileConverter implements FileConverter {
    private final JpegToPdfService jpegToPdfService;

    @Override
    public void convertToPDF(MultipartFile jpegFile, String outputFile) {
        if (jpegFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }
        
        var pdfFile = new File(outputFile);
        try {
            jpegToPdfService.convertJpegToPdf(jpegFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JPEG to PDF: " + e.getMessage(), e);
        }
    }
}