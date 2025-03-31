package com.xtopdf.xtopdf.converters;

import java.io.File;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import com.xtopdf.xtopdf.services.DocxToPdfService;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Component
public class DocxFileConverter implements FileConverter {
    private final DocxToPdfService docxToPdfService;

    @Override
    public void convertToPDF(MultipartFile docxFile, String outputFile) {
        docxToPdfService.convertDocxToPdf(docxFile, new File(outputFile));
    }
    
}
