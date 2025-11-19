package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
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
    
    public void convertWrlToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse VRML file
            VrmlFileData vrmlData = parseVrmlFile(inputFile);
            
            // Add title
            document.add(new Paragraph("VRML Scene Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: VRML (Virtual Reality Modeling Language)").setFontSize(12));
            document.add(new Paragraph("Version: " + vrmlData.version).setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add scene statistics
            document.add(new Paragraph("Scene Statistics:").setFontSize(14));
            document.add(new Paragraph("Total Nodes: " + vrmlData.totalNodes).setFontSize(12));
            document.add(new Paragraph("Shapes: " + vrmlData.shapeCount).setFontSize(12));
            document.add(new Paragraph("Transforms: " + vrmlData.transformCount).setFontSize(12));
            document.add(new Paragraph("Materials: " + vrmlData.materialCount).setFontSize(12));
            
            if (!vrmlData.nodeTypes.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Node Types Found:").setFontSize(12));
                for (Map.Entry<String, Integer> entry : vrmlData.nodeTypes.entrySet()) {
                    document.add(new Paragraph("  • " + entry.getKey() + ": " + entry.getValue()).setFontSize(10));
                }
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains scene statistics. For full 3D visualization, use VRML viewers or convert to X3D.").setFontSize(10));
            
            document.close();
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
