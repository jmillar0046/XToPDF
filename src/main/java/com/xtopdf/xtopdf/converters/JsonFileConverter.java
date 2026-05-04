package com.xtopdf.xtopdf.converters;

import com.xtopdf.xtopdf.exceptions.FileConversionException;

import com.xtopdf.xtopdf.services.conversion.data.JsonToPdfService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
@Component
public class JsonFileConverter implements FileConverter {
    private final JsonToPdfService jsonToPdfService;

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".json");
    }

    @Override
    public void convertToPDF(MultipartFile jsonFile, String outputFile) throws FileConversionException {
        if (jsonFile == null) {
            throw new FileConversionException("Input file must not be null");
        }
        if (outputFile == null) {
            throw new FileConversionException("Output file path must not be null");
        }
        try {
            var pdfFile = new File(outputFile);
            jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);        } catch (Exception e) {
            throw new FileConversionException("Error converting JSON to PDF: " + e.getMessage(), e);
        }
    }
}