package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.OdsToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class OdsFileConverter implements FileConverter {
    private final OdsToPdfService odsToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".ods");
    }

    @Override
    public void convertToPDF(MultipartFile odsFile, String outputFile) throws FileConversionException {
        if (odsFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            odsToPdfService.convertOdsToPdf(odsFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting ODS to PDF: " + e.getMessage(), e);
        }
    }
}