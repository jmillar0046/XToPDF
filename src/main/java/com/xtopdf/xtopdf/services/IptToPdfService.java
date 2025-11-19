package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert Inventor Part files (IPT) to PDF.
 */
@AllArgsConstructor
@Service
public class IptToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertIptToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use Autodesk Inventor to export to PDF or STEP", "Use Inventor View for visualization"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "Inventor Part", "IPT", suggestions);
    }
}
