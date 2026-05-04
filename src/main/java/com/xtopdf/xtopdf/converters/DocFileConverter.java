package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.document.DocToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class DocFileConverter implements FileConverter {
    private final DocToPdfService docToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".doc");
    }

    @Override
    public void convertToPDF(MultipartFile docFile, String outputFile) throws FileConversionException {
        if (docFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            docToPdfService.convertDocToPdf(docFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting DOC to PDF: " + e.getMessage(), e);
        }
    }
}