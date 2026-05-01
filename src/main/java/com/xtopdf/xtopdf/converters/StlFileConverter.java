package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            stlToPdfService.convertStlToPdf(stlFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Stl to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
