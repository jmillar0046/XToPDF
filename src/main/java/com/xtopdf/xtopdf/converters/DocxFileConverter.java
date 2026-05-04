package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.util.Set;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.conversion.document.DocxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DocxFileConverter implements FileConverter {
    private final DocxToPdfService docxToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".docx");
    }

    @Override
    public void convertToPDF(MultipartFile docxFile, String outputFile) throws FileConversionException {
        if (docxFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            docxToPdfService.convertDocxToPdf(docxFile, new File(outputFile));        } catch (Exception e) {
            throw new FileConversionException("Error converting DOCX to PDF: " + e.getMessage(), e);
        }
    }
}
