package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.data.XmlToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class XmlFileConverter implements FileConverter {
    private final XmlToPdfService xmlToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".xml");
    }

    @Override
    public void convertToPDF(MultipartFile xmlFile, String outputFile) throws FileConversionException {
        if (xmlFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            xmlToPdfService.convertXmlToPdf(xmlFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting XML to PDF: " + e.getMessage(), e);
        }
    }
}