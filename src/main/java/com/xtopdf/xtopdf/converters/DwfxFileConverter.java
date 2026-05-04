package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DwfxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwfxFileConverter implements FileConverter {
    private final DwfxToPdfService dwfxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwfx");
    }

    @Override
    public void convertToPDF(MultipartFile dwfxFile, String outputFile) throws FileConversionException {
        if (dwfxFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            dwfxToPdfService.convertDwfxToPdf(dwfxFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting DWFX to PDF: " + e.getMessage(), e);
        }
    }
}
