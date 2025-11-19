package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.XTToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class XTFileConverter implements FileConverter {
    private final XTToPdfService xtToPdfService;

    @Override
    public void convertToPDF(MultipartFile xtFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            xtToPdfService.convertXTToPdf(xtFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XT to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
