package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;

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
    
    private final PdfBackendProvider pdfBackend;
    
    public StepToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertStepToPdf(MultipartFile stepFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            // Parse STEP file
            StepFileData stepData = parseStepFile(stepFile);
            
            // Add title
            builder.addParagraph("STEP File Analysis\n\n");
            
            // Add file information
            builder.addParagraph("File: " + stepFile.getOriginalFilename());
            builder.addParagraph("Format: STEP (ISO 10303)");
            builder.addParagraph("");
            
            // Add header information
            if (!stepData.header.isEmpty()) {
                builder.addParagraph("Header Information:");
                for (String line : stepData.header) {
                    builder.addParagraph(line);
                }
                builder.addParagraph("");
            }
            
            // Add entity summary
            builder.addParagraph("Entity Summary:");
            builder.addParagraph("Total Entities: " + stepData.entityCount);
            
            // Add first few entities as sample
            if (!stepData.entities.isEmpty()) {
                builder.addParagraph("");
                builder.addParagraph("Sample Entities (first 50):");
                int count = Math.min(50, stepData.entities.size());
                for (int i = 0; i < count; i++) {
                    builder.addParagraph(stepData.entities.get(i));
                }
            }
            
            builder.save(pdfFile);
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
