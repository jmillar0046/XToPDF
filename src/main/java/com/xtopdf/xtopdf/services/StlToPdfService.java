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
 * Service to convert STL files to PDF.
 * STL (Stereolithography) is a 3D mesh format used for 3D printing.
 * This converter parses the STL file and provides mesh statistics.
 */
@Service
public class StlToPdfService {
    
    public void convertStlToPdf(MultipartFile stlFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse STL file
            StlFileData stlData = parseStlFile(stlFile);
            
            // Add title
            document.add(new Paragraph("STL 3D Model Analysis")
                .setFontSize(18)
                
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + stlFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: STL (Stereolithography)").setFontSize(12));
            document.add(new Paragraph("Type: " + (stlData.isBinary ? "Binary" : "ASCII")).setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add mesh statistics
            document.add(new Paragraph("Mesh Statistics:").setFontSize(14));
            document.add(new Paragraph("Triangle Count: " + stlData.triangleCount).setFontSize(12));
            document.add(new Paragraph("Vertex Count: " + (stlData.triangleCount * 3)).setFontSize(12));
            
            if (stlData.boundingBox != null) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Bounding Box:").setFontSize(12));
                document.add(new Paragraph(String.format("  X: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox[0], stlData.boundingBox[1], 
                    stlData.boundingBox[1] - stlData.boundingBox[0])).setFontSize(10));
                document.add(new Paragraph(String.format("  Y: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox[2], stlData.boundingBox[3], 
                    stlData.boundingBox[3] - stlData.boundingBox[2])).setFontSize(10));
                document.add(new Paragraph(String.format("  Z: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox[4], stlData.boundingBox[5], 
                    stlData.boundingBox[5] - stlData.boundingBox[4])).setFontSize(10));
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains mesh statistics. For full 3D visualization, please use specialized 3D viewing software.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting STL to PDF: " + e.getMessage(), e);
        }
    }
    
    private StlFileData parseStlFile(MultipartFile file) throws IOException {
        StlFileData data = new StlFileData();
        
        byte[] bytes = file.getBytes();
        
        // Check if binary or ASCII
        if (bytes.length > 80 && !new String(bytes, 0, 5).trim().toLowerCase().startsWith("solid")) {
            data.isBinary = true;
            parseBinaryStl(bytes, data);
        } else {
            data.isBinary = false;
            parseAsciiStl(file, data);
        }
        
        return data;
    }
    
    private void parseBinaryStl(byte[] bytes, StlFileData data) throws IOException {
        if (bytes.length < 84) {
            throw new IOException("Invalid binary STL file: too short");
        }
        
        // Skip 80-byte header
        ByteBuffer buffer = ByteBuffer.wrap(bytes, 80, bytes.length - 80);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Read triangle count
        data.triangleCount = buffer.getInt();
        
        // Initialize bounding box
        float[] bbox = new float[]{Float.MAX_VALUE, -Float.MAX_VALUE, 
                                   Float.MAX_VALUE, -Float.MAX_VALUE, 
                                   Float.MAX_VALUE, -Float.MAX_VALUE};
        
        // Read triangles (only first few to calculate bounding box)
        int trianglesToRead = Math.min(data.triangleCount, 1000);
        for (int i = 0; i < trianglesToRead && buffer.remaining() >= 50; i++) {
            // Skip normal (3 floats)
            buffer.getFloat();
            buffer.getFloat();
            buffer.getFloat();
            
            // Read 3 vertices
            for (int v = 0; v < 3; v++) {
                float x = buffer.getFloat();
                float y = buffer.getFloat();
                float z = buffer.getFloat();
                
                bbox[0] = Math.min(bbox[0], x);
                bbox[1] = Math.max(bbox[1], x);
                bbox[2] = Math.min(bbox[2], y);
                bbox[3] = Math.max(bbox[3], y);
                bbox[4] = Math.min(bbox[4], z);
                bbox[5] = Math.max(bbox[5], z);
            }
            
            // Skip attribute byte count
            buffer.getShort();
        }
        
        data.boundingBox = bbox;
    }
    
    private void parseAsciiStl(MultipartFile file, StlFileData data) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.startsWith("facet")) {
                    data.triangleCount++;
                }
            }
        }
    }
    
    private static class StlFileData {
        boolean isBinary = false;
        int triangleCount = 0;
        float[] boundingBox = null;
    }
}
