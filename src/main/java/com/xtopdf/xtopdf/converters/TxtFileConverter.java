package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.TxtToPdfService;

@Component
public class TxtFileConverter implements FileConverter {

    @Autowired
    private TxtToPdfService txtToPdfService;

    @Override
    public void convertToPDF(String inputFile, String outputFile) {
        var txtFile = new File(inputFile);
        var pdfFile = new File(outputFile);
    try {
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
    } catch (IOException e) {
        // bad file path
    }
    }
    
}
