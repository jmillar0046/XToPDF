package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DrwToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DrwFileConverter implements FileConverter {
    private final DrwToPdfService drwToPdfService;

    @Override
    public void convertToPDF(MultipartFile drwFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            drwToPdfService.convertDrwToPdf(drwFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Drw to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
