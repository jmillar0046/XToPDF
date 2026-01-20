package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Service to convert HP Graphics Language files (HPGL) to PDF.
 * HPGL and PLT are the same format - delegates to PltToPdfService.
 */
@AllArgsConstructor
@Slf4j
@Service
public class HpglToPdfService {
    private final PltToPdfService pltToPdfService;
    
    public void convertHpglToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        pltToPdfService.convertPltToPdf(inputFile, pdfFile);
    }
}
