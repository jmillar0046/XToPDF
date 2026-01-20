package com.xtopdf.xtopdf.services.conversion.cad;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

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
