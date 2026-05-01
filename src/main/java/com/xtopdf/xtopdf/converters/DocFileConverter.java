package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.document.DocToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
        var pdfFile = new File(outputFile);
        try {
            docToPdfService.convertDocToPdf(docFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOC to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}