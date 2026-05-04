package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.JpegToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class JpegFileConverter implements FileConverter {
    private final JpegToPdfService jpegToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".jpeg", ".jpg");
    }

    @Override
    public void convertToPDF(MultipartFile jpegFile, String outputFile) throws FileConversionException {
        if (jpegFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            jpegToPdfService.convertJpegToPdf(jpegFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting JPEG to PDF: " + e.getMessage(), e);
        }
    }
}