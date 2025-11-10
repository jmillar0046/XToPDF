package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.EpsToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class EpsFileConverter implements FileConverter {
    private final EpsToPdfService epsToPdfService;

    @Override
    public void convertToPDF(MultipartFile epsFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            epsToPdfService.convertEpsToPdf(epsFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting EPS to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
