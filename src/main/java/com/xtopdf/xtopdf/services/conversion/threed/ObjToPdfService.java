package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert OBJ files to PDF with 2D wireframe projection.
 * OBJ is a 3D geometry format.
 * Delegates wireframe rendering to {@link WireframeRenderer}.
 */
@Slf4j
@Service
public class ObjToPdfService {
    
    private final PdfBackendProvider pdfBackend;

    @Value("${xtopdf.max-3d-file-size:52428800}")
    private long maxFileSize;
    
    public ObjToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertObjToPdf(MultipartFile objFile, File pdfFile) throws IOException, FileConversionException {
        if (objFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        if (objFile.getSize() > maxFileSize) {
            throw new FileConversionException(
                    "OBJ file exceeds maximum size limit of " + maxFileSize + " bytes");
        }
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
                builder.addParagraph(String.format("  X: %.3f to %.3f", objData.boundingBox.minX(), objData.boundingBox.maxX()));
                builder.addParagraph(String.format("  Y: %.3f to %.3f", objData.boundingBox.minY(), objData.boundingBox.maxY()));
                builder.addParagraph(String.format("  Z: %.3f to %.3f", objData.boundingBox.minZ(), objData.boundingBox.maxZ()));
            }
            
            // Render wireframe if we have data — delegate to WireframeRenderer
            if (!objData.vertices.isEmpty() && !objData.faces.isEmpty()) {
                builder.addParagraph("\n2D Wireframe Projection:\n");
                List<int[]> edges = buildFaceEdges(objData.faces, objData.vertices.size());
                WireframeRenderer.renderEdges(builder, objData.vertices, edges, objData.boundingBox, 900);
            }
            
            builder.addParagraph("\nNote: Showing 2D projection of 3D model.");
            builder.save(pdfFile);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error converting OBJ to PDF", e);
        }
    }
    
    /**
     * Builds edge list from faces. For each face, creates edges between consecutive vertices.
     */
    List<int[]> buildFaceEdges(List<int[]> faces, int vertexCount) {
        List<int[]> edges = new ArrayList<>();
        for (int[] face : faces) {
            for (int j = 0; j < face.length; j++) {
                int v1Idx = face[j];
                int v2Idx = face[(j + 1) % face.length];
                if (v1Idx >= 0 && v1Idx < vertexCount && v2Idx >= 0 && v2Idx < vertexCount) {
                    edges.add(new int[]{v1Idx, v2Idx});
                }
            }
        }
        return edges;
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
                            data.boundingBox = data.boundingBox == null
                                    ? BoundingBox3D.initial(x, y, z)
                                    : data.boundingBox.expand(x, y, z);
                            data.vertexCount++;
                        } catch (NumberFormatException e) {
                            // Skip invalid vertex
                        }
                    }
                } else if (line.startsWith("f ")) {
                    // Face
                    String[] parts = line.split("\\s+");
                    int[] faceIndices = new int[parts.length - 1];
                    boolean validFace = true;
                    for (int i = 1; i < parts.length; i++) {
                        try {
                            String[] indexParts = parts[i].split("/");
                            int vIdx = Integer.parseInt(indexParts[0]) - 1; // OBJ is 1-indexed
                            faceIndices[i - 1] = vIdx;
                        } catch (Exception e) {
                            validFace = false;
                            break;
                        }
                    }
                    if (validFace) {
                        data.faces.add(faceIndices);
                        data.faceCount++;
                    }
                }
            }
        }
        
        return data;
    }
    
    static class ObjFileData {
        int vertexCount = 0;
        int faceCount = 0;
        BoundingBox3D boundingBox = null;
        List<float[]> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
    }
}
