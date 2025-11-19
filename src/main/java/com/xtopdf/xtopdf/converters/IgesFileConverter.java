package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.IgesToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IgesFileConverter implements FileConverter {
    private final IgesToPdfService igesToPdfService;

    @Override
    public void convertToPDF(MultipartFile igesFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            igesToPdfService.convertIgesToPdf(igesFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Iges to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
