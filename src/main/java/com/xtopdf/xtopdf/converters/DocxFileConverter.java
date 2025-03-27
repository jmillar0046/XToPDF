package com.xtopdf.xtopdf.converters;

import java.io.File;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocxToPdfService;

@AllArgsConstructor
@Component
public class DocxFileConverter implements FileConverter {
    private final DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(String inputFile, String outputFile) {
        docxToPdfService.convertDocxToPdf(new File(inputFile), new File(outputFile));
    }
    
}
