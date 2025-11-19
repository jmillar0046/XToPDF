package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert STP files to PDF.
 * STP is an alias for STEP format.
 */
@AllArgsConstructor
@Service
public class StpToPdfService {
    private final StepToPdfService stepToPdfService;
    
    public void convertStpToPdf(MultipartFile stpFile, File pdfFile) throws IOException {
        stepToPdfService.convertStepToPdf(stpFile, pdfFile);
    }
}
