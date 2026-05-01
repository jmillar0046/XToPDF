package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            dwgToPdfService.convertDwgToPdf(dwgFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DWG to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
