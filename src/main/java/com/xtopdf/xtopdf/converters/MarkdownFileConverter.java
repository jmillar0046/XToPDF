package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.document.MarkdownToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class MarkdownFileConverter implements FileConverter {
    private final MarkdownToPdfService markdownToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".md", ".markdown");
    }

    @Override
    public void convertToPDF(MultipartFile markdownFile, String outputFile) throws FileConversionException {
        var pdfFile = new File(outputFile);
        try {
            markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Markdown to PDF: " + e.getMessage(), e);
        }
    }
}