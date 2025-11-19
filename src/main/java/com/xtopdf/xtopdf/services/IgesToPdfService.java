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

/**
 * Service to convert IGES files to PDF.
 * IGES (Initial Graphics Exchange Specification) is a CAD data exchange format.
 */
@Service
public class IgesToPdfService {
    
    public void convertIgesToPdf(MultipartFile igesFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse IGES file
            IgesFileData igesData = parseIgesFile(igesFile);
            
            // Add title
            document.add(new Paragraph("IGES File Analysis")
                .setFontSize(18)
                
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + igesFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: IGES (Initial Graphics Exchange Specification)").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add global section data
            if (!igesData.globalSection.isEmpty()) {
                document.add(new Paragraph("Global Section:").setFontSize(14));
                for (String line : igesData.globalSection.subList(0, Math.min(10, igesData.globalSection.size()))) {
                    document.add(new Paragraph(line).setFontSize(9));
                }
                if (igesData.globalSection.size() > 10) {
                    document.add(new Paragraph("... (" + (igesData.globalSection.size() - 10) + " more lines)").setFontSize(9));
                }
                document.add(new Paragraph(""));
            }
            
            // Add entity statistics
            document.add(new Paragraph("Entity Statistics:").setFontSize(14));
            document.add(new Paragraph("Directory Entries: " + igesData.directoryEntryCount).setFontSize(12));
            document.add(new Paragraph("Parameter Data Lines: " + igesData.parameterDataCount).setFontSize(12));
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This is a parsed representation of the IGES file. For full CAD visualization, please use specialized CAD software.").setFontSize(10));
            
            document.close();
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
