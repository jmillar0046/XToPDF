package com.xtopdf.xtopdf.converters;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.io.exceptions.IOException;
import com.xtopdf.xtopdf.services.DocxToPdfService;

@Component
public class DocxFileConverter implements FileConverter {

    @Autowired
    private DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(String inputFile, String outputFile) {
        var docxFile = new File(inputFile);
        var pdfFile = new File(outputFile);

        try {
            docxToPdfService.convertDocxToPdf(docxFile, pdfFile);
        } catch (IOException e) {
            //bad file path
        }
    }
    
}
