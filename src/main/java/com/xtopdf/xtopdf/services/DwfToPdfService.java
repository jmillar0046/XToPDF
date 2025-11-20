package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service to convert Design Web Format files (DWF) to PDF.
 * DWF is a ZIP-based format containing drawing data and metadata.
 * This converter parses the DWF package and provides file statistics.
 */
@Service
public class DwfToPdfService {
    
    @Autowired
    PdfBackendProvider pdfBackend; // Package-private for testing
    
    public void convertDwfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        // Parse DWF file
        DwfFileData dwfData = parseDwfFile(inputFile);
        
        // Create PDF using abstraction layer
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Add title
            builder.addParagraph("DWF Package Analysis\n\n");
            
            // Add file information
            builder.addParagraph("File: " + inputFile.getOriginalFilename() + "\n");
            builder.addParagraph("Format: DWF (Design Web Format)\n\n");
            
            // Add package statistics
            builder.addParagraph("Package Statistics:\n");
            builder.addParagraph("Total Files: " + dwfData.totalFiles + "\n");
            builder.addParagraph("Sections: " + dwfData.sections.size() + "\n\n");
            
            if (!dwfData.sections.isEmpty()) {
                builder.addParagraph("Package Contents:\n");
                for (String section : dwfData.sections) {
                    builder.addParagraph("  • " + section + "\n");
                }
                builder.addParagraph("\n");
            }
            
            if (dwfData.hasDescriptor) {
                builder.addParagraph("Contains descriptor file with drawing metadata\n\n");
            }
            
            builder.addParagraph("Note: This PDF contains package statistics. For full viewing, use Autodesk Design Review or convert using specialized DWF tools.");
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting DWF to PDF: " + e.getMessage(), e);
        }
    }
    
    private DwfFileData parseDwfFile(MultipartFile file) throws IOException {
        DwfFileData data = new DwfFileData();
        
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                data.totalFiles++;
                String name = entry.getName();
                data.sections.add(name);
                
                // Check for descriptor file
                if (name.toLowerCase().contains("descriptor") || name.toLowerCase().endsWith(".xml")) {
                    data.hasDescriptor = true;
                }
            }
        } catch (Exception e) {
            throw new IOException("Error parsing DWF file: " + e.getMessage(), e);
        }
        
        return data;
    }
    
    private static class DwfFileData {
        int totalFiles = 0;
        List<String> sections = new ArrayList<>();
        boolean hasDescriptor = false;
    }
}
