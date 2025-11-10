package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.RtfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class RtfFileConverter implements FileConverter {
    private final RtfToPdfService rtfToPdfService;

    @Override
    public void convertToPDF(MultipartFile rtfFile, String outputFile) {
        if (rtfFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }
        
        var pdfFile = new File(outputFile);
        try {
            rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting RTF to PDF: " + e.getMessage(), e);
        }
    }
}