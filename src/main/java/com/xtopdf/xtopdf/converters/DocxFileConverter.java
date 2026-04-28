package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

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
    public void convertToPDF(MultipartFile docxFile, String outputFile) throws FileConversionException {
        try {
            docxToPdfService.convertDocxToPdf(docxFile, new File(outputFile));
        } catch (IOException e) {
            throw new FileConversionException("DOCX to PDF conversion failed: " + e.getMessage(), e);
        }
    }
}
