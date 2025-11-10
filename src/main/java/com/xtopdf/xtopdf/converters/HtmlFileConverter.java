package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.services.HtmlToPdfService;
import com.xtopdf.xtopdf.services.PageNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class HtmlFileConverter implements FileConverter {
    private final HtmlToPdfService htmlToPdfService;
    private final PageNumberService pageNumberService;

    @Override
    public void convertToPDF(MultipartFile htmlFile, String outputFile) {
        if (htmlFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        htmlToPdfService.convertHtmlToPdf(htmlFile, new File(outputFile));
    }
    
    @Override
    public void convertToPDF(MultipartFile htmlFile, String outputFile, PageNumberConfig pageNumberConfig) {
        if (htmlFile == null) {
            throw new NullPointerException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new NullPointerException("Output file must not be null");
        }

        var pdfFile = new File(outputFile);
        try {
            htmlToPdfService.convertHtmlToPdf(htmlFile, pdfFile);
            pageNumberService.addPageNumbers(pdfFile, pageNumberConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error converting HTML to PDF: " + e.getMessage(), e);
        }
    }
}
