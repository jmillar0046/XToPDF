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
