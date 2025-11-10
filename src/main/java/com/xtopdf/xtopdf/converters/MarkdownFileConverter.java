package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.MarkdownToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class MarkdownFileConverter implements FileConverter {
    private final MarkdownToPdfService markdownToPdfService;

    @Override
    public void convertToPDF(MultipartFile markdownFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Markdown to PDF: " + e.getMessage(), e);
        }
    }
    
}
