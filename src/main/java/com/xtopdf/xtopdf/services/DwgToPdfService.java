package com.xtopdf.xtopdf.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Service to convert DWG files to PDF by going through DXF intermediate format.
 * This follows the conversion path: DWG → DXF → PDF
 */
@AllArgsConstructor
@Service
public class DwgToPdfService {
    private final DwgToDxfService dwgToDxfService;
    private final DxfToPdfService dxfToPdfService;
    
    public void convertDwgToPdf(MultipartFile dwgFile, File pdfFile) throws IOException {
        // Create a temporary DXF file for the intermediate conversion
        File tempDxfFile = File.createTempFile("temp_dwg_to_dxf_", ".dxf");
        
        try {
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
                    return tempDxfFile.getName();
                }
                
                @Override
                public String getContentType() {
                    return "application/dxf";
                }
                
                @Override
                public boolean isEmpty() {
                    return tempDxfFile.length() == 0;
                }
                
                @Override
                public long getSize() {
                    return tempDxfFile.length();
                }
                
                @Override
                public byte[] getBytes() throws IOException {
                    try (FileInputStream fis = new FileInputStream(tempDxfFile)) {
                        return fis.readAllBytes();
                    }
                }
                
                @Override
                public InputStream getInputStream() throws IOException {
                    return new FileInputStream(tempDxfFile);
                }
                
                @Override
                public void transferTo(File dest) throws IOException {
                    try (FileInputStream fis = new FileInputStream(tempDxfFile);
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                        fis.transferTo(fos);
                    }
                }
            };
            
            dxfToPdfService.convertDxfToPdf(dxfMultipartFile, pdfFile);
        } finally {
            // Clean up temporary file
            if (tempDxfFile.exists()) {
                tempDxfFile.delete();
            }
        }
    }
}
