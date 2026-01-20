package com.xtopdf.xtopdf.services.conversion.cad;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Service to convert DWG files to PDF by going through DXF intermediate format.
 * This follows the conversion path: DWG → DXF → PDF
 */
@Slf4j
@AllArgsConstructor
@Service
public class DwgToPdfService {
    private final DwgToDxfService dwgToDxfService;
    private final DxfToPdfService dxfToPdfService;
    
    public void convertDwgToPdf(MultipartFile dwgFile, File pdfFile) throws IOException {
        File tempDxfFile = null;
        
        try {
            // Create a temporary DXF file for the intermediate conversion
            tempDxfFile = File.createTempFile("temp_dwg_to_dxf_", ".dxf");
            final File finalTempDxfFile = tempDxfFile; // Make effectively final for inner class
            
            // Step 1: Convert DWG to DXF
            dwgToDxfService.convertDwgToDxf(dwgFile, tempDxfFile);
            
            // Step 2: Convert DXF to PDF
            // Create a wrapper MultipartFile from the temporary DXF file
            MultipartFile dxfMultipartFile = new MultipartFile() {
                @Override
                public String getName() {
                    return "file";
                }
                
                @Override
                public String getOriginalFilename() {
                    return finalTempDxfFile.getName();
                }
                
                @Override
                public String getContentType() {
                    return "application/dxf";
                }
                
                @Override
                public boolean isEmpty() {
                    return finalTempDxfFile.length() == 0;
                }
                
                @Override
                public long getSize() {
                    return finalTempDxfFile.length();
                }
                
                @Override
                public byte[] getBytes() throws IOException {
                    try (FileInputStream fis = new FileInputStream(finalTempDxfFile)) {
                        return fis.readAllBytes();
                    }
                }
                
                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(finalTempDxfFile);
                }
                
                @Override
                public void transferTo(File dest) throws IOException {
                    try (FileInputStream fis = new FileInputStream(finalTempDxfFile);
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                        fis.transferTo(fos);
                    }
                }
            };
            
            dxfToPdfService.convertDxfToPdf(dxfMultipartFile, pdfFile);
        } finally {
            // Guaranteed cleanup of temporary file
            if (tempDxfFile != null && tempDxfFile.exists()) {
                if (!tempDxfFile.delete()) {
                    log.warn("Failed to delete temporary DXF file: {}", tempDxfFile.getAbsolutePath());
                }
            }
        }
    }
}
