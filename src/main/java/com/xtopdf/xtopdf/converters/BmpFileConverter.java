package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.BmpToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@AllArgsConstructor
@Component
public class BmpFileConverter implements FileConverter {
    private final BmpToPdfService bmpToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".bmp");
    }

    @Override
    public void convertToPDF(MultipartFile bmpFile, String outputFile) throws FileConversionException {
        if (bmpFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        var pdfFile = new File(outputFile);
        try {
            bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting BMP to PDF: " + e.getMessage(), e);
        }
    }
}