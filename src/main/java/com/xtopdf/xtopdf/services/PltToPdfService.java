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
 * Service to convert HPGL Plotter files (PLT) to PDF.
 * HPGL is a text-based plotter command language.
 * This converter parses the HPGL commands and provides drawing statistics.
 */
@Service
public class PltToPdfService {
    
    public void convertPltToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse PLT/HPGL file
            HpglFileData hpglData = parseHpglFile(inputFile);
            
            // Add title
            document.add(new Paragraph("HPGL/PLT Drawing Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: HPGL/PLT (Plotter Language)").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add drawing statistics
            document.add(new Paragraph("Drawing Statistics:").setFontSize(14));
            document.add(new Paragraph("Total Commands: " + hpglData.totalCommands).setFontSize(12));
            document.add(new Paragraph("Pen Movements: " + hpglData.penMovements).setFontSize(12));
            document.add(new Paragraph("Draw Commands: " + hpglData.drawCommands).setFontSize(12));
            document.add(new Paragraph("Label Commands: " + hpglData.labelCommands).setFontSize(12));
            document.add(new Paragraph("Pen Selects: " + hpglData.penSelects).setFontSize(12));
            
            if (!hpglData.commandTypes.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Command Types:").setFontSize(12));
                List<Map.Entry<String, Integer>> sortedCommands = new ArrayList<>(hpglData.commandTypes.entrySet());
                sortedCommands.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                for (int i = 0; i < Math.min(10, sortedCommands.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedCommands.get(i);
                    document.add(new Paragraph("  • " + entry.getKey() + ": " + entry.getValue()).setFontSize(10));
                }
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains command statistics. For visual rendering, use HPGL viewers like ViewCompanion or convert to PDF through specialized tools.").setFontSize(10));
            
            document.close();
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
