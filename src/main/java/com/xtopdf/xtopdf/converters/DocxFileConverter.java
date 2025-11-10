package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DocxFileConverter implements FileConverter {
    private final DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(MultipartFile docxFile, String outputFile) {
        convertToPDF(docxFile, outputFile, false);
    }
    
    @Override
    public void convertToPDF(MultipartFile docxFile, String outputFile, boolean executeMacros) {
        try {
            docxToPdfService.convertDocxToPdf(docxFile, new File(outputFile), executeMacros);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOCX to PDF: " + e.getMessage(), e);
        }
    }
}