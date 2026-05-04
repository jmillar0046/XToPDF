package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.presentation.PptToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class PptFileConverter implements FileConverter {
    private final PptToPdfService pptToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".ppt");
    }

    @Override
    public void convertToPDF(MultipartFile pptFile, String outputFile) throws FileConversionException {
        if (pptFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            pptToPdfService.convertPptToPdf(pptFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting PPT to PDF: " + e.getMessage(), e);
        }
    }
}