package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.presentation.PptxToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@AllArgsConstructor
@Component
public class PptxFileConverter implements FileConverter {
    private final PptxToPdfService pptxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".pptx");
    }

    @Override
    public void convertToPDF(MultipartFile pptxFile, String outputFile) throws FileConversionException {
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