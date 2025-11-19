package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.IdwToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class IdwFileConverter implements FileConverter {
    private final IdwToPdfService idwToPdfService;

    @Override
    public void convertToPDF(MultipartFile idwFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            idwToPdfService.convertIdwToPdf(idwFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Idw to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
