package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to convert STP files to PDF.
 * STP is an alias for STEP format.
 */
@AllArgsConstructor
@Slf4j
@Service
public class StpToPdfService {
    private final StepToPdfService stepToPdfService;
    
    public void convertStpToPdf(MultipartFile stpFile, File pdfFile) throws IOException {
        stepToPdfService.convertStepToPdf(stpFile, pdfFile);
    }
}
