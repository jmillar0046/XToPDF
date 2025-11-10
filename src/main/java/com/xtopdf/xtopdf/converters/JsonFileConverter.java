package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.JsonToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class JsonFileConverter implements FileConverter {
    private final JsonToPdfService jsonToPdfService;

    @Override
    public void convertToPDF(MultipartFile jsonFile, String outputFile) {
        var pdfFile = new File(outputFile);
        try {
            jsonToPdfService.convertJsonToPdf(jsonFile, pdfFile);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to PDF: " + e.getMessage(), e);
        } catch (NullPointerException e) {
            throw new NullPointerException("Input file or output file must not be null");
        }
    }
}
