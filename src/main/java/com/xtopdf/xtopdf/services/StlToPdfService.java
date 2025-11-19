package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert STL files to PDF with 2D wireframe projection.
 * STL (Stereolithography) is a 3D mesh format used for 3D printing.
 */
@Service
public class StlToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public StlToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertStlToPdf(MultipartFile stlFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Parse STL file
            StlFileData stlData = parseStlFile(stlFile);
            
            // Add title and info
            builder.addParagraph("STL 3D Model Rendering\n\n");
            builder.addParagraph("File: " + stlFile.getOriginalFilename());
            builder.addParagraph("Format: STL (Stereolithography)");
            builder.addParagraph("Type: " + (stlData.isBinary ? "Binary" : "ASCII"));
            builder.addParagraph("\nMesh Statistics:");
            builder.addParagraph("Triangle Count: " + stlData.triangleCount);
            builder.addParagraph("Vertex Count: " + (stlData.triangleCount * 3));
            
            if (stlData.boundingBox != null) {
                builder.addParagraph("\nBounding Box:");
                builder.addParagraph(String.format("  X: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox[0], stlData.boundingBox[1], 
                    stlData.boundingBox[1] - stlData.boundingBox[0]));
                builder.addParagraph(String.format("  Y: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox[2], stlData.boundingBox[3], 
                    stlData.boundingBox[3] - stlData.boundingBox[2]));
                builder.addParagraph(String.format("  Z: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox[4], stlData.boundingBox[5], 
                    stlData.boundingBox[5] - stlData.boundingBox[4]));
            }
            
            // Render 2D projection if we have vertices
            if (!stlData.vertices.isEmpty()) {
                builder.addParagraph("\n2D Wireframe Projection (Top View):\n");
                renderWireframe(builder, stlData);
            }
            
            builder.addParagraph("\nNote: Showing 2D projection of 3D model. For full 3D visualization, use specialized 3D viewing software.");
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting STL to PDF: " + e.getMessage(), e);
        }
    }
    
    private void renderWireframe(PdfDocumentBuilder builder, StlFileData stlData) throws IOException {
        if (stlData.vertices.isEmpty() || stlData.boundingBox == null) {
            return;
        }
        
        // Calculate scaling to fit in PDF (use 400x400 area)
        float renderWidth = 400;
        float renderHeight = 400;
        float offsetX = 100;
        float offsetY = 350; // Y-axis inverted in PDF
        
        float modelWidth = stlData.boundingBox[1] - stlData.boundingBox[0];
        float modelHeight = stlData.boundingBox[3] - stlData.boundingBox[2];
        
        float scale = Math.min(renderWidth / Math.max(modelWidth, 0.001f), 
                               renderHeight / Math.max(modelHeight, 0.001f)) * 0.9f;
        
        // Draw sample of triangles (limit to avoid overwhelming the PDF)
        int maxTriangles = Math.min(stlData.triangleCount, 200);
        int step = Math.max(1, stlData.triangleCount / maxTriangles);
        
        for (int i = 0; i < stlData.triangleCount; i += step) {
            if (i * 3 + 2 >= stlData.vertices.size()) break;
            
            float[] v1 = stlData.vertices.get(i * 3);
            float[] v2 = stlData.vertices.get(i * 3 + 1);
            float[] v3 = stlData.vertices.get(i * 3 + 2);
            
            // Project to 2D (top view - use X and Y coordinates)
            float x1 = offsetX + (v1[0] - stlData.boundingBox[0]) * scale;
            float y1 = offsetY - (v1[1] - stlData.boundingBox[2]) * scale;
            float x2 = offsetX + (v2[0] - stlData.boundingBox[0]) * scale;
            float y2 = offsetY - (v2[1] - stlData.boundingBox[2]) * scale;
            float x3 = offsetX + (v3[0] - stlData.boundingBox[0]) * scale;
            float y3 = offsetY - (v3[1] - stlData.boundingBox[2]) * scale;
            
            // Draw triangle edges
            builder.drawLine(x1, y1, x2, y2);
            builder.drawLine(x2, y2, x3, y3);
            builder.drawLine(x3, y3, x1, y1);
        }
    }
    
    private StlFileData parseStlFile(MultipartFile file) throws IOException {
        StlFileData data = new StlFileData();
        byte[] bytes = file.getBytes();
        
        if (bytes.length < 84) {
            return data;
        }
        
        // Check if binary STL
        String header = new String(bytes, 0, Math.min(80, bytes.length));
        if (!header.trim().toLowerCase().startsWith("solid")) {
            // Binary STL
            data.isBinary = true;
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.position(80); // Skip header
            data.triangleCount = buffer.getInt();
            
            // Parse vertices
            for (int i = 0; i < data.triangleCount && buffer.remaining() >= 50; i++) {
                buffer.position(buffer.position() + 12); // Skip normal vector
                
                for (int v = 0; v < 3; v++) {
                    float x = buffer.getFloat();
                    float y = buffer.getFloat();
                    float z = buffer.getFloat();
                    data.vertices.add(new float[]{x, y, z});
                    data.updateBoundingBox(x, y, z);
                }
                
                buffer.position(buffer.position() + 2); // Skip attribute byte count
            }
        } else {
            // ASCII STL
            data.isBinary = false;
            String content = new String(bytes);
            String[] lines = content.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("facet")) {
                    data.triangleCount++;
                } else if (line.startsWith("vertex")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            float x = Float.parseFloat(parts[1]);
                            float y = Float.parseFloat(parts[2]);
                            float z = Float.parseFloat(parts[3]);
                            data.vertices.add(new float[]{x, y, z});
                            data.updateBoundingBox(x, y, z);
                        } catch (NumberFormatException e) {
                            // Skip invalid vertex
                        }
                    }
                }
            }
        }
        
        return data;
    }
    
    private static class StlFileData {
        boolean isBinary = false;
        int triangleCount = 0;
        float[] boundingBox = null;
        List<float[]> vertices = new ArrayList<>();
        
        void updateBoundingBox(float x, float y, float z) {
            if (boundingBox == null) {
                boundingBox = new float[]{x, x, y, y, z, z};
            } else {
                boundingBox[0] = Math.min(boundingBox[0], x);
                boundingBox[1] = Math.max(boundingBox[1], x);
                boundingBox[2] = Math.min(boundingBox[2], y);
                boundingBox[3] = Math.max(boundingBox[3], y);
                boundingBox[4] = Math.min(boundingBox[4], z);
                boundingBox[5] = Math.max(boundingBox[5], z);
            }
        }
    }
}
