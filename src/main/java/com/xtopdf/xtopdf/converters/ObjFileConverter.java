package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            objToPdfService.convertObjToPdf(objFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting Obj to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
