package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DwgToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwgFileConverter implements FileConverter {
    private final DwgToPdfService dwgToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwg");
    }

    @Override
    public void convertToPDF(MultipartFile dwgFile, String outputFile) throws FileConversionException {
        if (dwgFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting DWG to PDF: " + e.getMessage(), e);
        }
    }
}
