package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.PltToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class PltFileConverter implements FileConverter {
    private final PltToPdfService pltToPdfService;

    @Override
    public void convertToPDF(MultipartFile pltFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            pltToPdfService.convertPltToPdf(pltFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Plt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
