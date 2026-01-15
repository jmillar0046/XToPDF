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
 * Service to convert DWT (AutoCAD Template) files to PDF.
 * DWT files have the same structure as DWG files.
 */
@AllArgsConstructor
@Slf4j
@Service
public class DwtToPdfService {
    private final DwgToPdfService dwgToPdfService;
    
    public void convertDwtToPdf(MultipartFile dwtFile, File pdfFile) throws IOException {
        dwgToPdfService.convertDwgToPdf(dwtFile, pdfFile);
    }
}
