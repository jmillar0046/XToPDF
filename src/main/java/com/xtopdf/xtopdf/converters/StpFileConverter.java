package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.StpToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class StpFileConverter implements FileConverter {
    private final StpToPdfService stpToPdfService;

    @Override
    public void convertToPDF(MultipartFile stpFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            stpToPdfService.convertStpToPdf(stpFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Stp to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
