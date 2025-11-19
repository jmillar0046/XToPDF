package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.SldprtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class SldprtFileConverter implements FileConverter {
    private final SldprtToPdfService sldprtToPdfService;

    @Override
    public void convertToPDF(MultipartFile sldprtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            sldprtToPdfService.convertSldprtToPdf(sldprtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Sldprt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
