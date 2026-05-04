package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.image.WmfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class WmfFileConverter implements FileConverter {
    private final WmfToPdfService wmfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".wmf");
    }

    @Override
    public void convertToPDF(MultipartFile wmfFile, String outputFile) throws FileConversionException {
        if (wmfFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            wmfToPdfService.convertWmfToPdf(wmfFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting WMF to PDF: " + e.getMessage(), e);
        }
    }
}
