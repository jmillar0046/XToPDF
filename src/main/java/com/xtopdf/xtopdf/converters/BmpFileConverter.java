package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.BmpToPdfService;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class BmpFileConverter implements FileConverter {
    private final BmpToPdfService bmpToPdfService;
    private final PageNumberService pageNumberService;

    @Override
    public void convertToPDF(MultipartFile bmpFile, String outputFile) {
        if (bmpFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        var pdfFile = new File(outputFile);
        try {
            bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting BMP to PDF: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void convertToPDF(MultipartFile bmpFile, String outputFile, PageNumberConfig pageNumberConfig) {
        if (bmpFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        var pdfFile = new File(outputFile);
        try {
            bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting BMP to PDF: " + e.getMessage(), e);
        }
    }
}