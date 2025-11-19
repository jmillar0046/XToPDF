package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.CatdrawingToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class CatdrawingFileConverter implements FileConverter {
    private final CatdrawingToPdfService catdrawingToPdfService;

    @Override
    public void convertToPDF(MultipartFile catdrawingFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            catdrawingToPdfService.convertCatdrawingToPdf(catdrawingFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Catdrawing to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
