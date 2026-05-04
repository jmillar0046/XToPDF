package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.spreadsheet.TsvToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class TsvFileConverter implements FileConverter {
    private final TsvToPdfService tsvToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".tsv", ".tab");
    }

    @Override
    public void convertToPDF(MultipartFile tsvFile, String outputFile) throws FileConversionException {
        if (tsvFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting TSV to PDF: " + e.getMessage(), e);
        }
    }
}
