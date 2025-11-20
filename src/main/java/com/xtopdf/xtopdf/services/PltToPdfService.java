package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Service to convert HPGL Plotter files (PLT) to PDF.
 * HPGL is a text-based plotter command language.
 * This converter parses the HPGL commands and provides drawing statistics.
 */
@Service
public class PltToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    @Autowired
    public PltToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertPltToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        // Parse PLT/HPGL file
        HpglFileData hpglData = parseHpglFile(inputFile);
        
        // Create PDF using abstraction layer
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Add title
            builder.addParagraph("HPGL/PLT Drawing Analysis\n\n");
            
            // Add file information
            builder.addParagraph("File: " + inputFile.getOriginalFilename() + "\n");
            builder.addParagraph("Format: HPGL/PLT (Plotter Language)\n\n");
            
            // Add drawing statistics
            builder.addParagraph("Drawing Statistics:\n");
            builder.addParagraph("Total Commands: " + hpglData.totalCommands + "\n");
            builder.addParagraph("Pen Movements: " + hpglData.penMovements + "\n");
            builder.addParagraph("Draw Commands: " + hpglData.drawCommands + "\n");
            builder.addParagraph("Label Commands: " + hpglData.labelCommands + "\n");
            builder.addParagraph("Pen Selects: " + hpglData.penSelects + "\n\n");
            
            if (!hpglData.commandTypes.isEmpty()) {
                builder.addParagraph("Command Types:\n");
                List<Map.Entry<String, Integer>> sortedCommands = new ArrayList<>(hpglData.commandTypes.entrySet());
                sortedCommands.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                for (int i = 0; i < Math.min(10, sortedCommands.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedCommands.get(i);
                    builder.addParagraph("  • " + entry.getKey() + ": " + entry.getValue() + "\n");
                }
                builder.addParagraph("\n");
            }
            
            builder.addParagraph("Note: This PDF contains command statistics. For visual rendering, use HPGL viewers like ViewCompanion or convert to PDF through specialized tools.");
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting PLT to PDF: " + e.getMessage(), e);
        }
    }
    
    private HpglFileData parseHpglFile(MultipartFile file) throws IOException {
        HpglFileData data = new HpglFileData();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            
            String hpglContent = content.toString();
            
            // Parse HPGL commands (format: XX or XX####;)
            int i = 0;
            while (i < hpglContent.length() - 1) {
                // Skip whitespace
                while (i < hpglContent.length() && Character.isWhitespace(hpglContent.charAt(i))) {
                    i++;
                }
                
                if (i >= hpglContent.length() - 1) break;
                
                // Read command (2 letters)
                if (Character.isLetter(hpglContent.charAt(i)) && i + 1 < hpglContent.length() && 
                    Character.isLetter(hpglContent.charAt(i + 1))) {
                    
                    String command = hpglContent.substring(i, i + 2);
                    data.totalCommands++;
                    data.commandTypes.put(command, data.commandTypes.getOrDefault(command, 0) + 1);
                    
                    // Count specific command types
                    switch (command) {
                        case "PU": // Pen Up
                        case "PD": // Pen Down
                            data.penMovements++;
                            break;
                        case "PA": // Plot Absolute
                        case "PR": // Plot Relative
                        case "AA": // Arc Absolute
                        case "AR": // Arc Relative
                            data.drawCommands++;
                            break;
                        case "LB": // Label
                            data.labelCommands++;
                            break;
                        case "SP": // Select Pen
                            data.penSelects++;
                            break;
                    }
                    
                    i += 2;
                    
                    // Skip parameters until semicolon or next command
                    while (i < hpglContent.length() && hpglContent.charAt(i) != ';' && 
                           !Character.isLetter(hpglContent.charAt(i))) {
                        i++;
                    }
                    
                    // Skip semicolon
                    if (i < hpglContent.length() && hpglContent.charAt(i) == ';') {
                        i++;
                    }
                } else {
                    i++;
                }
            }
            
        }
        
        return data;
    }
    
    private static class HpglFileData {
        int totalCommands = 0;
        int penMovements = 0;
        int drawCommands = 0;
        int labelCommands = 0;
        int penSelects = 0;
        Map<String, Integer> commandTypes = new HashMap<>();
    }
}
