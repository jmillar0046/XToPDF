package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.IgsToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IgsFileConverter implements FileConverter {
    private final IgsToPdfService igsToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".igs");
    }

    @Override
    public void convertToPDF(MultipartFile igsFile, String outputFile) throws FileConversionException {
        if (igsFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            igsToPdfService.convertIgsToPdf(igsFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting IGS to PDF: " + e.getMessage(), e);
        }
    }
}
