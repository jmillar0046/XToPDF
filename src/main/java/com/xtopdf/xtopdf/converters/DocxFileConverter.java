package com.xtopdf.xtopdf.converters;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocxToPdfService;

@Component
public class DocxFileConverter implements FileConverter {

    @Autowired
    private DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(String inputFile, String outputFile) {
        docxToPdfService.convertDocxToPdf(new File(inputFile), new File(outputFile));
    }
    
}
