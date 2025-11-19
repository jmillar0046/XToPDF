package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.HpglToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class HpglFileConverter implements FileConverter {
    private final HpglToPdfService hpglToPdfService;

    @Override
    public void convertToPDF(MultipartFile hpglFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            hpglToPdfService.convertHpglToPdf(hpglFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Hpgl to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
