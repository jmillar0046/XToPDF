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
 * Service to convert Design Web Format XPS files (DWFX) to PDF.
 * DWFX is based on the XPS format, delegates to DwfToPdfService for similar parsing.
 */
@AllArgsConstructor
@Slf4j
@Service
public class DwfxToPdfService {
    private final DwfToPdfService dwfToPdfService;
    
    public void convertDwfxToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        dwfToPdfService.convertDwfToPdf(inputFile, pdfFile);
    }
}
