package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.PptToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class PptFileConverter implements FileConverter {
    private final PptToPdfService pptToPdfService;

    @Override
    public void convertToPDF(MultipartFile pptFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            pptToPdfService.convertPptToPdf(pptFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting PPT to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
