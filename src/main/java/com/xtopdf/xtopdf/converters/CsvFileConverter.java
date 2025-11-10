package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.PageNumberService;
import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.CsvToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class CsvFileConverter implements FileConverter {
    private final CsvToPdfService csvToPdfService;
    private final PageNumberService pageNumberService;

    @Override
    public void convertToPDF(MultipartFile csvFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting CSV to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }

    @Override
    public void convertToPDF(MultipartFile csvFile, String outputFile, PageNumberConfig pageNumberConfig) {
        var pdfFile = new File(outputFile);
        try {
            csvToPdfService.convertCsvToPdf(csvFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting CSV to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
