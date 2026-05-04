package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.ObjToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class ObjFileConverter implements FileConverter {
    private final ObjToPdfService objToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".obj");
    }

    @Override
    public void convertToPDF(MultipartFile objFile, String outputFile) throws FileConversionException {
        if (objFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            objToPdfService.convertObjToPdf(objFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting OBJ to PDF: " + e.getMessage(), e);
        }
    }
}
