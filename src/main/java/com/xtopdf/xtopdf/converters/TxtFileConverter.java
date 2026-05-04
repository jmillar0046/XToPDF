package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.document.TxtToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class TxtFileConverter implements FileConverter {
    private final TxtToPdfService txtToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".txt");
    }

    @Override
    public void convertToPDF(MultipartFile txtFile, String outputFile) throws FileConversionException {
        if (txtFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            txtToPdfService.convertTxtToPdf(txtFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting TXT to PDF: " + e.getMessage(), e);
        }
    }
}