package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
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
    
    public void convertDwfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse DWF file
            DwfFileData dwfData = parseDwfFile(inputFile);
            
            // Add title
            document.add(new Paragraph("DWF Package Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: DWF (Design Web Format)").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add package statistics
            document.add(new Paragraph("Package Statistics:").setFontSize(14));
            document.add(new Paragraph("Total Files: " + dwfData.totalFiles).setFontSize(12));
            document.add(new Paragraph("Sections: " + dwfData.sections.size()).setFontSize(12));
            
            if (!dwfData.sections.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Package Contents:").setFontSize(12));
                for (String section : dwfData.sections) {
                    document.add(new Paragraph("  • " + section).setFontSize(10));
                }
            }
            
            if (dwfData.hasDescriptor) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Contains descriptor file with drawing metadata").setFontSize(10));
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains package statistics. For full viewing, use Autodesk Design Review or convert using specialized DWF tools.").setFontSize(10));
            
            document.close();
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
