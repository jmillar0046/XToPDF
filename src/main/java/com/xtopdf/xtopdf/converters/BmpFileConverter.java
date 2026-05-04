package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.BmpToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            bmpToPdfService.convertBmpToPdf(bmpFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting BMP to PDF: " + e.getMessage(), e);
        }
    }
}