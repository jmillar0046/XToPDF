package com.xtopdf.xtopdf.converters;

import java.io.File;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocxToPdfService;

@AllArgsConstructor
@Component
public class DocxFileConverter implements FileConverter {
    private final DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(File docxFile, String outputFile) {
        docxToPdfService.convertDocxToPdf(docxFile, new File(outputFile));
    }
    
}
