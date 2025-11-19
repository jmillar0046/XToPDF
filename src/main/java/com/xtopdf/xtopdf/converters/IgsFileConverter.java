package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.IgsToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IgsFileConverter implements FileConverter {
    private final IgsToPdfService igsToPdfService;

    @Override
    public void convertToPDF(MultipartFile igsFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            igsToPdfService.convertIgsToPdf(igsFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Igs to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
