package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.XlsxToPdfService;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class XlsxFileConverter implements FileConverter {
    private final XlsxToPdfService xlsxToPdfService;
    private final PageNumberService pageNumberService;

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

    @Override
    public void convertToPDF(MultipartFile xlsxFile, String outputFile, PageNumberConfig pageNumberConfig) {
        var pdfFile = new File(outputFile);
        try {
            xlsxToPdfService.convertXlsxToPdf(xlsxFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XLSX to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
