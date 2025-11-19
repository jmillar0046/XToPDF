package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert IGES files to PDF.
 * IGES (Initial Graphics Exchange Specification) is a CAD data exchange format.
 */
@Service
public class IgesToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public IgesToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertIgesToPdf(MultipartFile igesFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            // Parse IGES file
            IgesFileData igesData = parseIgesFile(igesFile);
            
            // Add title
            builder.addParagraph("IGES File Analysis\n\n");
                
            
            // Add file information
            builder.addParagraph("File: " + igesFile.getOriginalFilename());
            builder.addParagraph("Format: IGES (Initial Graphics Exchange Specification)");
            builder.addParagraph("");
            
            // Add global section data
            if (!igesData.globalSection.isEmpty()) {
                builder.addParagraph("Global Section:");
                for (String line : igesData.globalSection.subList(0, Math.min(10, igesData.globalSection.size()))) {
                    builder.addParagraph(line);
                }
                if (igesData.globalSection.size() > 10) {
                    builder.addParagraph("... (" + (igesData.globalSection.size() - 10) + " more lines)");
                }
                builder.addParagraph("");
            }
            
            // Add entity statistics
            builder.addParagraph("Entity Statistics:");
            builder.addParagraph("Directory Entries: " + igesData.directoryEntryCount);
            builder.addParagraph("Parameter Data Lines: " + igesData.parameterDataCount);
            
            builder.addParagraph("");
            builder.addParagraph("Note: This is a parsed representation of the IGES file. For full CAD visualization, please use specialized CAD software.");
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting IGES to PDF: " + e.getMessage(), e);
        }
    }
    
    private IgesFileData parseIgesFile(MultipartFile file) throws IOException {
        IgesFileData data = new IgesFileData();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 72) continue;
                
                // IGES lines have a section identifier in column 73
                char section = line.length() >= 73 ? line.charAt(72) : ' ';
                String content = line.substring(0, Math.min(72, line.length())).trim();
                
                switch (section) {
                    case 'S': // Start section
                        data.startSection.add(content);
                        break;
                    case 'G': // Global section
                        data.globalSection.add(content);
                        break;
                    case 'D': // Directory Entry section
                        data.directoryEntryCount++;
                        break;
                    case 'P': // Parameter Data section
                        data.parameterDataCount++;
                        break;
                    case 'T': // Terminate section
                        data.terminateSection.add(content);
                        break;
                }
            }
        }
        
        return data;
    }
    
    private static class IgesFileData {
        List<String> startSection = new ArrayList<>();
        List<String> globalSection = new ArrayList<>();
        List<String> terminateSection = new ArrayList<>();
        int directoryEntryCount = 0;
        int parameterDataCount = 0;
    }
}
