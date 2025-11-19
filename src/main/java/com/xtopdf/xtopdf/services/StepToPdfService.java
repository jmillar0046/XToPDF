package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to convert STEP/STP files to PDF.
 * STEP (Standard for the Exchange of Product Data) is a text-based CAD format.
 * This converter parses the STEP file and extracts entity information.
 */
@Service
public class StepToPdfService {
    
    public void convertStepToPdf(MultipartFile stepFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse STEP file
            StepFileData stepData = parseStepFile(stepFile);
            
            // Add title
            document.add(new Paragraph("STEP File Analysis")
                .setFontSize(18)
                
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + stepFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: STEP (ISO 10303)").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add header information
            if (!stepData.header.isEmpty()) {
                document.add(new Paragraph("Header Information:").setFontSize(14));
                for (String line : stepData.header) {
                    document.add(new Paragraph(line).setFontSize(10));
                }
                document.add(new Paragraph(""));
            }
            
            // Add entity summary
            document.add(new Paragraph("Entity Summary:").setFontSize(14));
            document.add(new Paragraph("Total Entities: " + stepData.entityCount).setFontSize(12));
            
            // Add first few entities as sample
            if (!stepData.entities.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Sample Entities (first 50):").setFontSize(12));
                int count = Math.min(50, stepData.entities.size());
                for (int i = 0; i < count; i++) {
                    document.add(new Paragraph(stepData.entities.get(i)).setFontSize(8));
                }
            }
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting STEP to PDF: " + e.getMessage(), e);
        }
    }
    
    private StepFileData parseStepFile(MultipartFile file) throws IOException {
        StepFileData data = new StepFileData();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            boolean inHeader = false;
            boolean inData = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.equals("HEADER;")) {
                    inHeader = true;
                    continue;
                } else if (line.equals("ENDSEC;") && inHeader) {
                    inHeader = false;
                    continue;
                } else if (line.equals("DATA;")) {
                    inData = true;
                    continue;
                } else if (line.equals("ENDSEC;") && inData) {
                    inData = false;
                    break;
                }
                
                if (inHeader && !line.isEmpty()) {
                    data.header.add(line);
                } else if (inData && line.startsWith("#")) {
                    data.entities.add(line);
                    data.entityCount++;
                }
            }
        }
        
        return data;
    }
    
    private static class StepFileData {
        List<String> header = new ArrayList<>();
        List<String> entities = new ArrayList<>();
        int entityCount = 0;
    }
}
