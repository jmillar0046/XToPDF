package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.JtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class JtFileConverter implements FileConverter {
    private final JtToPdfService jtToPdfService;

    @Override
    public void convertToPDF(MultipartFile jtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            jtToPdfService.convertJtToPdf(jtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Jt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
