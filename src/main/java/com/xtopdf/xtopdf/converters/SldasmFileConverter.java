package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.SldasmToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class SldasmFileConverter implements FileConverter {
    private final SldasmToPdfService sldasmToPdfService;

    @Override
    public void convertToPDF(MultipartFile sldasmFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            sldasmToPdfService.convertSldasmToPdf(sldasmFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Sldasm to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
