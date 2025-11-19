package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.SkpToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class SkpFileConverter implements FileConverter {
    private final SkpToPdfService skpToPdfService;

    @Override
    public void convertToPDF(MultipartFile skpFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            skpToPdfService.convertSkpToPdf(skpFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Skp to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
