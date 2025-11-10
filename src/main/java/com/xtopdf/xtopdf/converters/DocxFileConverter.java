package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DocxFileConverter implements FileConverter {
    private final DocxToPdfService docxToPdfService;
    private final PageNumberService pageNumberService;

    @Override
    public void convertToPDF(MultipartFile docxFile, String outputFile) {
        try {
            docxToPdfService.convertDocxToPdf(docxFile, new File(outputFile));
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOCX to PDF: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void convertToPDF(MultipartFile docxFile, String outputFile, PageNumberConfig pageNumberConfig) {
        var pdfFile = new File(outputFile);
        try {
            docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOCX to PDF: " + e.getMessage(), e);
        }
    }
    
}
