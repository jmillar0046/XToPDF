package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.XlsToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class XlsFileConverter implements FileConverter {
    private final XlsToPdfService xlsToPdfService;

    @Override
    public void convertToPDF(MultipartFile xlsFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            xlsToPdfService.convertXlsToPdf(xlsFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XLS to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
