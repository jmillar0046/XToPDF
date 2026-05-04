package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.document.RtfToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class RtfFileConverter implements FileConverter {
    private final RtfToPdfService rtfToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".rtf");
    }

    @Override
    public void convertToPDF(MultipartFile rtfFile, String outputFile) throws FileConversionException {
        if (rtfFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            rtfToPdfService.convertRtfToPdf(rtfFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting RTF to PDF: " + e.getMessage(), e);
        }
    }
}