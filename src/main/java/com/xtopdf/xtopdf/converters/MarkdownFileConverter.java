package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
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
        if (markdownFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            markdownToPdfService.convertMarkdownToPdf(markdownFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting Markdown to PDF: " + e.getMessage(), e);
        }
    }
}