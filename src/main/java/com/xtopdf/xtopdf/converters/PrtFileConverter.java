package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.PrtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class PrtFileConverter implements FileConverter {
    private final PrtToPdfService prtToPdfService;

    @Override
    public void convertToPDF(MultipartFile prtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            prtToPdfService.convertPrtToPdf(prtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Prt to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
