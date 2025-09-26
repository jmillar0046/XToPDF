package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.XlsxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class XlsxFileConverter implements FileConverter {
    private final XlsxToPdfService xlsxToPdfService;

    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile) {
        try {
            xlsxToPdfService.convertXlsxToPdf(xlsxFile, new File(outputFile));
        } catch (IOException e) {
            throw new RuntimeException("Error converting XLSX to PDF: " + e.getMessage(), e);
        }
    }
}