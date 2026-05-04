package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.StpToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class StpFileConverter implements FileConverter {
    private final StpToPdfService stpToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".stp");
    }

    @Override
    public void convertToPDF(MultipartFile stpFile, String outputFile) throws FileConversionException {
        if (stpFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            stpToPdfService.convertStpToPdf(stpFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting STP to PDF: " + e.getMessage(), e);
        }
    }
}
