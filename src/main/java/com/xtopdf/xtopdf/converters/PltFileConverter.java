package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.PltToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class PltFileConverter implements FileConverter {
    private final PltToPdfService pltToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".plt");
    }

    @Override
    public void convertToPDF(MultipartFile pltFile, String outputFile) throws FileConversionException {
        if (pltFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            pltToPdfService.convertPltToPdf(pltFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting PLT to PDF: " + e.getMessage(), e);
        }
    }
}
