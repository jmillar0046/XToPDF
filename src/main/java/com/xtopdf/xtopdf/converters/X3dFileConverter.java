package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.X3dToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class X3dFileConverter implements FileConverter {
    private final X3dToPdfService x3dToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".x3d");
    }

    @Override
    public void convertToPDF(MultipartFile x3dFile, String outputFile) throws FileConversionException {
        if (x3dFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            x3dToPdfService.convertX3dToPdf(x3dFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting X3D to PDF: " + e.getMessage(), e);
        }
    }
}
