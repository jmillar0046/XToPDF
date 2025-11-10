package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.OdsToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class OdsFileConverter implements FileConverter {
    private final OdsToPdfService odsToPdfService;

    @Override
    public void convertToPDF(MultipartFile odsFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            odsToPdfService.convertOdsToPdf(odsFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ODS to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
