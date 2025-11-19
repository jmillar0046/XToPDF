package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert OBJ files to PDF with 2D wireframe projection.
 * OBJ is a 3D geometry format.
 */
@Service
public class ObjToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    public ObjToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertObjToPdf(MultipartFile objFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            ObjFileData objData = parseObjFile(objFile);
            
            builder.addParagraph("OBJ 3D Model Rendering\n\n");
            builder.addParagraph("File: " + objFile.getOriginalFilename());
            builder.addParagraph("Format: OBJ (Wavefront)");
            builder.addParagraph("\nModel Statistics:");
            builder.addParagraph("Vertices: " + objData.vertexCount);
            builder.addParagraph("Faces: " + objData.faceCount);
            
            if (objData.boundingBox != null) {
                builder.addParagraph("\nBounding Box:");
                builder.addParagraph(String.format("  X: %.3f to %.3f", objData.boundingBox[0], objData.boundingBox[1]));
                builder.addParagraph(String.format("  Y: %.3f to %.3f", objData.boundingBox[2], objData.boundingBox[3]));
                builder.addParagraph(String.format("  Z: %.3f to %.3f", objData.boundingBox[4], objData.boundingBox[5]));
            }
            
            // Render wireframe if we have data
            if (!objData.vertices.isEmpty() && !objData.faces.isEmpty()) {
                builder.addParagraph("\n2D Wireframe Projection:\n");
                renderWireframe(builder, objData);
            }
            
            builder.addParagraph("\nNote: Showing 2D projection of 3D model.");
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error converting OBJ to PDF: " + e.getMessage(), e);
        }
    }
    
    private void renderWireframe(PdfDocumentBuilder builder, ObjFileData objData) throws IOException {
        if (objData.vertices.isEmpty() || objData.boundingBox == null) return;
        
        float renderWidth = 400;
        float renderHeight = 400;
        float offsetX = 100;
        float offsetY = 350;
        
        float modelWidth = objData.boundingBox[1] - objData.boundingBox[0];
        float modelHeight = objData.boundingBox[3] - objData.boundingBox[2];
        float scale = Math.min(renderWidth / Math.max(modelWidth, 0.001f), 
                               renderHeight / Math.max(modelHeight, 0.001f)) * 0.9f;
        
        // Draw faces (limit to avoid overwhelming)
        int maxFaces = Math.min(objData.faces.size(), 300);
        for (int i = 0; i < maxFaces; i++) {
            int[] face = objData.faces.get(i);
            for (int j = 0; j < face.length; j++) {
                int v1Idx = face[j];
                int v2Idx = face[(j + 1) % face.length];
                
                if (v1Idx < objData.vertices.size() && v2Idx < objData.vertices.size()) {
                    float[] v1 = objData.vertices.get(v1Idx);
                    float[] v2 = objData.vertices.get(v2Idx);
                    
                    float x1 = offsetX + (v1[0] - objData.boundingBox[0]) * scale;
                    float y1 = offsetY - (v1[1] - objData.boundingBox[2]) * scale;
                    float x2 = offsetX + (v2[0] - objData.boundingBox[0]) * scale;
                    float y2 = offsetY - (v2[1] - objData.boundingBox[2]) * scale;
                    
                    builder.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }
    
    private ObjFileData parseObjFile(MultipartFile file) throws IOException {
        ObjFileData data = new ObjFileData();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    // Vertex
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            float x = Float.parseFloat(parts[1]);
                            float y = Float.parseFloat(parts[2]);
                            float z = Float.parseFloat(parts[3]);
                            data.vertices.add(new float[]{x, y, z});
                            data.updateBoundingBox(x, y, z);
                            data.vertexCount++;
                        } catch (NumberFormatException e) {
                            // Skip invalid vertex
                        }
                    }
                } else if (line.startsWith("f ")) {
                    // Face
                    String[] parts = line.split("\\s+");
                    int[] faceIndices = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        try {
                            String[] indexParts = parts[i].split("/");
                            int vIdx = Integer.parseInt(indexParts[0]) - 1; // OBJ is 1-indexed
                            faceIndices[i - 1] = vIdx;
                        } catch (Exception e) {
                            faceIndices[i - 1] = 0;
                        }
                    }
                    data.faces.add(faceIndices);
                    data.faceCount++;
                }
            }
        }
        
        return data;
    }
    
    private static class ObjFileData {
        int vertexCount = 0;
        int faceCount = 0;
        float[] boundingBox = null;
        List<float[]> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        
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
