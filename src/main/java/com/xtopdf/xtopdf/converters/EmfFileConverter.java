package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.EmfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class EmfFileConverter implements FileConverter {
    private final EmfToPdfService emfToPdfService;

    @Override
    public void convertToPDF(MultipartFile emfFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            emfToPdfService.convertEmfToPdf(emfFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Emf to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
