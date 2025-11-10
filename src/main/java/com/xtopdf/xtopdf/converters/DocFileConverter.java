package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DocFileConverter implements FileConverter {
    private final DocToPdfService docToPdfService;

    @Override
    public void convertToPDF(MultipartFile docFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            docToPdfService.convertDocToPdf(docFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOC to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
