package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.document.OdtToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class OdtFileConverter implements FileConverter {
    private final OdtToPdfService odtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".odt");
    }

    @Override
    public void convertToPDF(MultipartFile odtFile, String outputFile) throws FileConversionException {
        if (odtFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            odtToPdfService.convertOdtToPdf(odtFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting ODT to PDF: " + e.getMessage(), e);
        }
    }
}