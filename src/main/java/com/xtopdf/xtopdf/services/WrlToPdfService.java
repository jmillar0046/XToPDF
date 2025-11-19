package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Service to convert VRML files (WRL) to PDF.
 * VRML (Virtual Reality Modeling Language) is a text-based 3D scene format.
 * This converter parses the VRML file and provides scene statistics.
 */
@Service
public class WrlToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public WrlToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertWrlToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            // Parse VRML file
            VrmlFileData vrmlData = parseVrmlFile(inputFile);
            
            // Add title
            builder.addParagraph("VRML Scene Analysis\n\n");
            
            // Add file information
            builder.addParagraph("File: " + inputFile.getOriginalFilename());
            builder.addParagraph("Format: VRML (Virtual Reality Modeling Language)");
            builder.addParagraph("Version: " + vrmlData.version);
            builder.addParagraph("");
            
            // Add scene statistics
            builder.addParagraph("Scene Statistics:");
            builder.addParagraph("Total Nodes: " + vrmlData.totalNodes);
            builder.addParagraph("Shapes: " + vrmlData.shapeCount);
            builder.addParagraph("Transforms: " + vrmlData.transformCount);
            builder.addParagraph("Materials: " + vrmlData.materialCount);
            
            if (!vrmlData.nodeTypes.isEmpty()) {
                builder.addParagraph("");
                builder.addParagraph("Node Types Found:");
                for (Map.Entry<String, Integer> entry : vrmlData.nodeTypes.entrySet()) {
                    builder.addParagraph("  • " + entry.getKey() + ": " + entry.getValue());
                }
            }
            
            builder.addParagraph("");
            builder.addParagraph("Note: This PDF contains scene statistics. For full 3D visualization, use VRML viewers or convert to X3D.");
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting VRML to PDF: " + e.getMessage(), e);
        }
    }
    
    private VrmlFileData parseVrmlFile(MultipartFile file) throws IOException {
        VrmlFileData data = new VrmlFileData();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {
            
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Parse version from first line
                if (firstLine) {
                    if (line.startsWith("#VRML")) {
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 2) {
                            data.version = parts[1];
                        }
                    }
                    firstLine = false;
                }
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Count node types
                if (line.contains("{")) {
                    String[] tokens = line.split("\\s+");
                    for (int i = 0; i < tokens.length - 1; i++) {
                        String token = tokens[i];
                        if (tokens[i + 1].equals("{") && !token.isEmpty() && Character.isUpperCase(token.charAt(0))) {
                            data.totalNodes++;
                            data.nodeTypes.put(token, data.nodeTypes.getOrDefault(token, 0) + 1);
                            
                            // Count specific types
                            if (token.equals("Shape")) data.shapeCount++;
                            else if (token.equals("Transform")) data.transformCount++;
                            else if (token.contains("Material")) data.materialCount++;
                        }
                    }
                }
            }
        }
        
        return data;
    }
    
    private static class VrmlFileData {
        String version = "Unknown";
        int totalNodes = 0;
        int shapeCount = 0;
        int transformCount = 0;
        int materialCount = 0;
        Map<String, Integer> nodeTypes = new HashMap<>();
    }
}
