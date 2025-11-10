package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.services.XmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@Component
public class XmlFileConverter implements FileConverter {
    private final XmlToPdfService xmlToPdfService;

    @Override
    public void convertToPDF(MultipartFile xmlFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting XML to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}