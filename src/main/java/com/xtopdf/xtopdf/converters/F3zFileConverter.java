package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.F3zToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class F3zFileConverter implements FileConverter {
    private final F3zToPdfService f3zToPdfService;

    @Override
    public void convertToPDF(MultipartFile f3zFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            f3zToPdfService.convertF3zToPdf(f3zFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting F3z to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
