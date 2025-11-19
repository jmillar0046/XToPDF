package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service to convert Windows Metafile files (WMF) to PDF.
 * WMF is a binary vector graphics format for Windows.
 * This converter parses the WMF header and provides file statistics.
 */
@Service
public class WmfToPdfService {
    
    public void convertWmfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse WMF file
            WmfFileData wmfData = parseWmfFile(inputFile);
            
            // Add title
            document.add(new Paragraph("Windows Metafile Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: WMF (Windows Metafile)").setFontSize(12));
            document.add(new Paragraph("Type: " + (wmfData.isPlaceable ? "Placeable" : "Standard")).setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add metafile statistics
            document.add(new Paragraph("Metafile Statistics:").setFontSize(14));
            document.add(new Paragraph("File Size: " + formatSize(inputFile.getSize())).setFontSize(12));
            
            if (wmfData.isPlaceable && wmfData.boundsValid) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Bounds:").setFontSize(12));
                document.add(new Paragraph(String.format("  Left: %d, Top: %d", wmfData.boundsLeft, wmfData.boundsTop)).setFontSize(10));
                document.add(new Paragraph(String.format("  Right: %d, Bottom: %d", wmfData.boundsRight, wmfData.boundsBottom)).setFontSize(10));
                document.add(new Paragraph(String.format("  Width: %d, Height: %d", 
                    wmfData.boundsRight - wmfData.boundsLeft, 
                    wmfData.boundsBottom - wmfData.boundsTop)).setFontSize(10));
            }
            
            if (wmfData.maxRecordSize > 0) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Max Record Size: " + wmfData.maxRecordSize + " words").setFontSize(12));
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains metafile statistics. For visual rendering, use image conversion tools or Windows applications.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting WMF to PDF: " + e.getMessage(), e);
        }
    }
    
    private WmfFileData parseWmfFile(MultipartFile file) throws IOException {
        WmfFileData data = new WmfFileData();
        
        byte[] bytes = file.getBytes();
        if (bytes.length < 18) { // Minimum WMF header size
            return data;
        }
        
        try {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // Check for placeable metafile header (magic number 0x9AC6CDD7)
            int magic = buffer.getInt();
            if (magic == 0x9AC6CDD7) {
                data.isPlaceable = true;
                
                // Skip handle (2 bytes)
                buffer.getShort();
                
                // Read bounding rectangle
                data.boundsLeft = buffer.getShort();
                data.boundsTop = buffer.getShort();
                data.boundsRight = buffer.getShort();
                data.boundsBottom = buffer.getShort();
                data.boundsValid = true;
                
                // Skip inch (2 bytes) and reserved (4 bytes) and checksum (2 bytes)
                buffer.position(buffer.position() + 8);
            } else {
                // Standard WMF, rewind
                buffer.position(0);
            }
            
            // Read standard WMF header
            int fileType = buffer.getShort() & 0xFFFF;
            int headerSize = buffer.getShort() & 0xFFFF;
            
            // Skip version (2 bytes)
            buffer.getShort();
            
            // Read file size in words
            int fileSizeWords = buffer.getInt();
            
            // Skip number of objects (2 bytes)
            buffer.getShort();
            
            // Read max record size
            data.maxRecordSize = buffer.getInt();
            
        } catch (Exception e) {
            // If parsing fails, return what we have
        }
        
        return data;
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " bytes";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    private static class WmfFileData {
        boolean isPlaceable = false;
        boolean boundsValid = false;
        int boundsLeft = 0;
        int boundsTop = 0;
        int boundsRight = 0;
        int boundsBottom = 0;
        int maxRecordSize = 0;
    }
}
