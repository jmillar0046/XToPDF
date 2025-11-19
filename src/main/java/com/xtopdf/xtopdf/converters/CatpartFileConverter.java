package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.CatpartToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class CatpartFileConverter implements FileConverter {
    private final CatpartToPdfService catpartToPdfService;

    @Override
    public void convertToPDF(MultipartFile catpartFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            catpartToPdfService.convertCatpartToPdf(catpartFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Catpart to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
