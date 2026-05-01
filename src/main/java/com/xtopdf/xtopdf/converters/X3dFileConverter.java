package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            x3dToPdfService.convertX3dToPdf(x3dFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting X3d to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
