package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.StepToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class StepFileConverter implements FileConverter {
    private final StepToPdfService stepToPdfService;

    @Override
    public void convertToPDF(MultipartFile stepFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            stepToPdfService.convertStepToPdf(stepFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Step to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
