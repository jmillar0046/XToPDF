package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.CsvToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class CsvFileConverter implements FileConverter {
    private final CsvToPdfService csvToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".csv");
    }

    @Override
    public void convertToPDF(MultipartFile csvFile, String outputFile) throws FileConversionException {
        if (csvFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting CSV to PDF: " + e.getMessage(), e);
        }
    }
}