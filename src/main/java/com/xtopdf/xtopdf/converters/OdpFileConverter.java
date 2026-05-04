package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.presentation.OdpToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class OdpFileConverter implements FileConverter {
    private final OdpToPdfService odpToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".odp");
    }

    @Override
    public void convertToPDF(MultipartFile odpFile, String outputFile) throws FileConversionException {
        if (odpFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            odpToPdfService.convertOdpToPdf(odpFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting ODP to PDF: " + e.getMessage(), e);
        }
    }
}