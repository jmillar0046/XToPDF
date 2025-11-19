package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.ThreeDmToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class ThreeDmFileConverter implements FileConverter {
    private final ThreeDmToPdfService threedmToPdfService;

    @Override
    public void convertToPDF(MultipartFile threedmFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            threedmToPdfService.convertThreeDmToPdf(threedmFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting ThreeDm to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
