package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DgnToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DgnFileConverter implements FileConverter {
    private final DgnToPdfService dgnToPdfService;

    @Override
    public void convertToPDF(MultipartFile dgnFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            dgnToPdfService.convertDgnToPdf(dgnFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Dgn to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
