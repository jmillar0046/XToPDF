package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert VRML files (WRL) to PDF.
 */
@AllArgsConstructor
@Service
public class WrlToPdfService {
    private final ProprietaryCadToPdfService proprietaryCadService;
    
    public void convertWrlToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        String[] suggestions = {"Use 3D viewers to export to OBJ or STL", "Convert to X3D then to other formats"};
        proprietaryCadService.convertToPdf(inputFile, pdfFile, "VRML", "WRL", suggestions);
    }
}
