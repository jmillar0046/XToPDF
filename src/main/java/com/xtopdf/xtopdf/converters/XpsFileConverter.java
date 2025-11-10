package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.XpsToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class XpsFileConverter implements FileConverter {
    private final XpsToPdfService xpsToPdfService;

    @Override
    public void convertToPDF(MultipartFile xpsFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            xpsToPdfService.convertXpsToPdf(xpsFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XPS to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
