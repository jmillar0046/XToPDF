package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.image.EmfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class EmfFileConverter implements FileConverter {
    private final EmfToPdfService emfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".emf");
    }

    @Override
    public void convertToPDF(MultipartFile emfFile, String outputFile) throws FileConversionException {
        if (emfFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            emfToPdfService.convertEmfToPdf(emfFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting EMF to PDF: " + e.getMessage(), e);
        }
    }
}
