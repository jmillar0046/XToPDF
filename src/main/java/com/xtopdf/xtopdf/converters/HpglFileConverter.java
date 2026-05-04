package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.HpglToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class HpglFileConverter implements FileConverter {
    private final HpglToPdfService hpglToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".hpgl");
    }

    @Override
    public void convertToPDF(MultipartFile hpglFile, String outputFile) throws FileConversionException {
        if (hpglFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            hpglToPdfService.convertHpglToPdf(hpglFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting HPGL to PDF: " + e.getMessage(), e);
        }
    }
}
