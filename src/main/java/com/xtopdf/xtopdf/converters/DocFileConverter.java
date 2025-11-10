package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.DocToPdfService;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class DocFileConverter implements FileConverter {
    private final DocToPdfService docToPdfService;
    private final PageNumberService pageNumberService;

    @Override
    public void convertToPDF(MultipartFile docFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            docToPdfService.convertDocToPdf(docFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOC to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }

    @Override
    public void convertToPDF(MultipartFile docFile, String outputFile, PageNumberConfig pageNumberConfig) {
        var pdfFile = new File(outputFile);
        try {
            docToPdfService.convertDocToPdf(docFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting DOC to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
