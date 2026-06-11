package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import lombok.extern.slf4j.Slf4j;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.springframework.beans.factory.annotation.Value;
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
 * Delegates wireframe rendering to {@link WireframeRenderer}.
 */
@Slf4j
@Service
public class StlToPdfService {
    
    private final PdfBackendProvider pdfBackend;

    @Value("${xtopdf.max-3d-file-size:52428800}")
    private long maxFileSize;

    public StlToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }

    public void convertStlToPdf(MultipartFile stlFile, File pdfFile) throws IOException, FileConversionException {
        if (stlFile == null) {
            throw new IOException("Input file must not be null");
        }
        if (pdfFile == null) {
            throw new IOException("Output file must not be null");
        }

        if (stlFile.getSize() > maxFileSize) {
            throw new FileConversionException(
                    "STL file exceeds maximum size limit of " + maxFileSize + " bytes");
        }
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
                    stlData.boundingBox.minX(), stlData.boundingBox.maxX(), 
                    stlData.boundingBox.width()));
                builder.addParagraph(String.format("  Y: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox.minY(), stlData.boundingBox.maxY(), 
                    stlData.boundingBox.height()));
                builder.addParagraph(String.format("  Z: %.3f to %.3f (size: %.3f)", 
                    stlData.boundingBox.minZ(), stlData.boundingBox.maxZ(), 
                    stlData.boundingBox.depth()));
            }
            
            // Render 2D projection if we have vertices — delegate to WireframeRenderer
            if (!stlData.vertices.isEmpty()) {
                builder.addParagraph("\n2D Wireframe Projection (Top View):\n");
                List<int[]> edges = buildTriangleEdges(stlData.triangleCount, stlData.vertices.size());
                WireframeRenderer.renderEdges(builder, stlData.vertices, edges, stlData.boundingBox, 600);
            }
            
            builder.addParagraph("\nNote: Showing 2D projection of 3D model. For full 3D visualization, use specialized 3D viewing software.");
            
            builder.save(pdfFile);

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error converting STL to PDF", e);
        }
    }
    
    /**
     * Builds edge list from triangle vertices. For each triangle i, edges are:
     * [i*3, i*3+1], [i*3+1, i*3+2], [i*3+2, i*3]
     */
    List<int[]> buildTriangleEdges(int triangleCount, int vertexCount) {
        List<int[]> edges = new ArrayList<>();
        for (int i = 0; i < triangleCount; i++) {
            int base = i * 3;
            if (base + 2 >= vertexCount) break;
            edges.add(new int[]{base, base + 1});
            edges.add(new int[]{base + 1, base + 2});
            edges.add(new int[]{base + 2, base});
        }
        return edges;
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
                    data.boundingBox = data.boundingBox == null
                            ? BoundingBox3D.initial(x, y, z)
                            : data.boundingBox.expand(x, y, z);
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
                            data.boundingBox = data.boundingBox == null
                                    ? BoundingBox3D.initial(x, y, z)
                                    : data.boundingBox.expand(x, y, z);
                        } catch (NumberFormatException e) {
                            // Skip invalid vertex
                        }
                    }
                }
            }
        }
        
        return data;
    }
    
    static class StlFileData {
        boolean isBinary = false;
        int triangleCount = 0;
        BoundingBox3D boundingBox = null;
        List<float[]> vertices = new ArrayList<>();
    }
}
