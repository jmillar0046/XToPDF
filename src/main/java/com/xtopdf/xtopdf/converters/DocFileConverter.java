package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.DocToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class DocFileConverter implements FileConverter {
    private final DocToPdfService docToPdfService;

    @Override
    public void convertToPDF(MultipartFile docFile, String outputFile) {
        convertToPDF(docFile, outputFile, false);
    }
    
    @Override
    public void convertToPDF(MultipartFile docFile, String outputFile, boolean executeMacros) {
        var pdfFile = new File(outputFile);
        try {
            docToPdfService.convertDocToPdf(docFile, pdfFile, executeMacros);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOC to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}