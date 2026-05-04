package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.cad.DxfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DxfFileConverter implements FileConverter {
    private final DxfToPdfService dxfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".dxf");
    }

    @Override
    public void convertToPDF(MultipartFile dxfFile, String outputFile) throws FileConversionException {
        if (dxfFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            dxfToPdfService.convertDxfToPdf(dxfFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting DXF to PDF: " + e.getMessage(), e);
        }
    }
}
