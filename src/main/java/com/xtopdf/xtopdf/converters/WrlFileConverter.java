package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.WrlToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class WrlFileConverter implements FileConverter {
    private final WrlToPdfService wrlToPdfService;

    @Override
    public void convertToPDF(MultipartFile wrlFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            wrlToPdfService.convertWrlToPdf(wrlFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Wrl to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
