package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.IgesToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IgesFileConverter implements FileConverter {
    private final IgesToPdfService igesToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".iges");
    }

    @Override
    public void convertToPDF(MultipartFile igesFile, String outputFile) throws FileConversionException {
        if (igesFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            igesToPdfService.convertIgesToPdf(igesFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting IGES to PDF: " + e.getMessage(), e);
        }
    }
}
