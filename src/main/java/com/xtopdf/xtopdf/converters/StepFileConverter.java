package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.StepToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class StepFileConverter implements FileConverter {
    private final StepToPdfService stepToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".step");
    }

    @Override
    public void convertToPDF(MultipartFile stepFile, String outputFile) throws FileConversionException {
        if (stepFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            stepToPdfService.convertStepToPdf(stepFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting STEP to PDF: " + e.getMessage(), e);
        }
    }
}
