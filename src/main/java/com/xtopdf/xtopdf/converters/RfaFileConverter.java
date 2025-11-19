package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.RfaToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class RfaFileConverter implements FileConverter {
    private final RfaToPdfService rfaToPdfService;

    @Override
    public void convertToPDF(MultipartFile rfaFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            rfaToPdfService.convertRfaToPdf(rfaFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Rfa to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
