package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert X3D files (X3D) to PDF.
 */
@AllArgsConstructor
@Service
public class X3dToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertX3dToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use 3D viewers to export to OBJ or STL", "Convert using X3D processing tools"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "X3D", "X3D", suggestions);
    }
}
