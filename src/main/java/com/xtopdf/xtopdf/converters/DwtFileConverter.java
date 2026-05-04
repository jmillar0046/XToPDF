package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DwtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwtFileConverter implements FileConverter {
    private final DwtToPdfService dwtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwt");
    }

    @Override
    public void convertToPDF(MultipartFile dwtFile, String outputFile) throws FileConversionException {
        if (dwtFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            dwtToPdfService.convertDwtToPdf(dwtFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting DWT to PDF: " + e.getMessage(), e);
        }
    }
}
