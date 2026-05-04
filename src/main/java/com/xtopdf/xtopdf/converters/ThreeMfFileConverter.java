package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.threed.ThreeMfToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class ThreeMfFileConverter implements FileConverter {
    private final ThreeMfToPdfService threemfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".3mf");
    }

    @Override
    public void convertToPDF(MultipartFile threemfFile, String outputFile) throws FileConversionException {
        if (threemfFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            threemfToPdfService.convert3mfToPdf(threemfFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting 3MF to PDF: " + e.getMessage(), e);
        }
    }
}
