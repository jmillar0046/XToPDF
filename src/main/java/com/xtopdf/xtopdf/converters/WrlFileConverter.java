package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.WrlToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class WrlFileConverter implements FileConverter {
    private final WrlToPdfService wrlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".wrl");
    }

    @Override
    public void convertToPDF(MultipartFile wrlFile, String outputFile) throws FileConversionException {
        if (wrlFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            wrlToPdfService.convertWrlToPdf(wrlFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting WRL to PDF: " + e.getMessage(), e);
        }
    }
}
