package com.xtopdf.xtopdf.converters;

import java.io.File;
import java.io.IOException;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.TxtToPdfService;

@AllArgsConstructor
@Component
public class TxtFileConverter implements FileConverter {
    private final TxtToPdfService txtToPdfService;

    @Override
    public void convertToPDF(File txtFile, String outputFile) {
        var pdfFile = new File(outputFile);
    try {
        txtToPdfService.convertTxtToPdf(txtFile, pdfFile);
    } catch (IOException e) {
        // bad file path
    }
    }
    
}
