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
        var pdfFile = new File(outputFile);
        try {
            pptxToPdfService.convertPptxToPdf(pptxFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting PPTX to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}