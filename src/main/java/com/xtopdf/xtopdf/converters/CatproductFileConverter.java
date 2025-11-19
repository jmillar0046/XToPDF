package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.CatproductToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class CatproductFileConverter implements FileConverter {
    private final CatproductToPdfService catproductToPdfService;

    @Override
    public void convertToPDF(MultipartFile catproductFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            catproductToPdfService.convertCatproductToPdf(catproductFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Catproduct to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
