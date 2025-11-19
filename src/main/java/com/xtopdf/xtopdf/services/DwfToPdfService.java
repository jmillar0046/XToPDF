package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Design Web Format files (DWF) to PDF.
 */
@AllArgsConstructor
@Service
public class DwfToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertDwfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Autodesk Design Review to print to PDF", "Use DWF to PDF online converters"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Design Web Format", "DWF", suggestions);
    }
}
