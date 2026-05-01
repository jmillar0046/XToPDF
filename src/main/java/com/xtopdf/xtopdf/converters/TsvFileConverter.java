package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            tsvToPdfService.convertTsvToPdf(tsvFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting TSV to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
