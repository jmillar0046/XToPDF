package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.SlddrawToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class SlddrawFileConverter implements FileConverter {
    private final SlddrawToPdfService slddrawToPdfService;

    @Override
    public void convertToPDF(MultipartFile slddrawFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            slddrawToPdfService.convertSlddrawToPdf(slddrawFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Slddraw to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
