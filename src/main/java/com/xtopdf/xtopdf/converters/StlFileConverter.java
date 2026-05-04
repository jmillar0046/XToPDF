package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.StlToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class StlFileConverter implements FileConverter {
    private final StlToPdfService stlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".stl");
    }

    @Override
    public void convertToPDF(MultipartFile stlFile, String outputFile) throws FileConversionException {
        if (stlFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            stlToPdfService.convertStlToPdf(stlFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting STL to PDF: " + e.getMessage(), e);
        }
    }
}
