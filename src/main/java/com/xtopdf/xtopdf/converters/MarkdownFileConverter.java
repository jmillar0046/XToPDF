package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.MarkdownToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class MarkdownFileConverter implements FileConverter {
    private final MarkdownToPdfService markdownToPdfService;
    private final PageNumberService pageNumberService;

    @Override
    public void convertToPDF(MultipartFile markdownFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Markdown to PDF: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void convertToPDF(MultipartFile markdownFile, String outputFile, PageNumberConfig pageNumberConfig) {
        var pdfFile = new File(outputFile);
        try {
            markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Markdown to PDF: " + e.getMessage(), e);
        }
    }
    
}
