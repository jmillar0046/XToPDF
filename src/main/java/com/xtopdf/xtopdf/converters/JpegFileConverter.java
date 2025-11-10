package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.JpegToPdfService;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class JpegFileConverter implements FileConverter {
    private final JpegToPdfService jpegToPdfService;
    private final PageNumberService pageNumberService;

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

    @Override
    public void convertToPDF(MultipartFile jpegFile, String outputFile, PageNumberConfig pageNumberConfig) {
        if (jpegFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }
        
        var pdfFile = new File(outputFile);
        try {
            jpegToPdfService.convertJpegToPdf(jpegFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JPEG to PDF: " + e.getMessage(), e);
        }
    }
}
