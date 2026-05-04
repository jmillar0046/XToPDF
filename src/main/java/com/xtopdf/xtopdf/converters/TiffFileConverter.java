package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.image.TiffToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class TiffFileConverter implements FileConverter {
    private final TiffToPdfService tiffToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".tiff", ".tif");
    }

    @Override
    public void convertToPDF(MultipartFile tiffFile, String outputFile) throws FileConversionException {
        if (tiffFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            tiffToPdfService.convertTiffToPdf(tiffFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting TIFF to PDF: " + e.getMessage(), e);
        }
    }
}