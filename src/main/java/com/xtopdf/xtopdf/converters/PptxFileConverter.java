package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.PptxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class PptxFileConverter implements FileConverter {
    private final PptxToPdfService pptxToPdfService;

    @Override
    public void convertToPDF(MultipartFile pptxFile, String outputFile) {
        if (pptxFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        try {
            pptxToPdfService.convertPptxToPdf(pptxFile, new File(outputFile));
        } catch (IOException e) {
            throw new RuntimeException("Error converting PPTX to PDF: " + e.getMessage(), e);
        }
    }
}