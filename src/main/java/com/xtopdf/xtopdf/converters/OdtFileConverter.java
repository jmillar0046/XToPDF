package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.OdtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class OdtFileConverter implements FileConverter {
    private final OdtToPdfService odtToPdfService;

    @Override
    public void convertToPDF(MultipartFile odtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            odtToPdfService.convertOdtToPdf(odtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ODT to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
