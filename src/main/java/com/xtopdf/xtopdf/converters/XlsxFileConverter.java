package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.XlsxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class XlsxFileConverter implements FileConverter {
    private final XlsxToPdfService xlsxToPdfService;

    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            xlsxToPdfService.convertXlsxToPdf(xlsxFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XLSX to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}