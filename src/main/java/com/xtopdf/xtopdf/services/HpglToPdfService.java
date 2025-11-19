package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert HP Graphics Language files (HPGL) to PDF.
 */
@AllArgsConstructor
@Service
public class HpglToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertHpglToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use ViewCompanion or similar HPGL viewers", "Convert to DXF first then to PDF"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "HP Graphics Language", "HPGL", suggestions);
    }
}
