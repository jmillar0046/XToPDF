package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DwfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DwfFileConverter implements FileConverter {
    private final DwfToPdfService dwfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dwf");
    }

    @Override
    public void convertToPDF(MultipartFile dwfFile, String outputFile) throws FileConversionException {
        if (dwfFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            dwfToPdfService.convertDwfToPdf(dwfFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting DWF to PDF: " + e.getMessage(), e);
        }
    }
}
