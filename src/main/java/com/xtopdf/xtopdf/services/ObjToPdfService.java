package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert OBJ files to PDF.
 * OBJ is a 3D geometry definition file format.
 * This converter parses the OBJ file and provides mesh statistics.
 */
@Service
public class ObjToPdfService {
    
    public void convertObjToPdf(MultipartFile objFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse OBJ file
            ObjFileData objData = parseObjFile(objFile);
            
            // Add title
            document.add(new Paragraph("OBJ 3D Model Analysis")
                .setFontSize(18)
                
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + objFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: Wavefront OBJ").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add mesh statistics
            document.add(new Paragraph("Mesh Statistics:").setFontSize(14));
            document.add(new Paragraph("Vertices: " + objData.vertexCount).setFontSize(12));
            document.add(new Paragraph("Texture Coordinates: " + objData.texCoordCount).setFontSize(12));
            document.add(new Paragraph("Normals: " + objData.normalCount).setFontSize(12));
            document.add(new Paragraph("Faces: " + objData.faceCount).setFontSize(12));
            
            if (!objData.groups.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Groups:").setFontSize(12));
                for (String group : objData.groups) {
                    document.add(new Paragraph("  • " + group).setFontSize(10));
                }
            }
            
            if (!objData.materials.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Materials Referenced:").setFontSize(12));
                for (String material : objData.materials) {
                    document.add(new Paragraph("  • " + material).setFontSize(10));
                }
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains mesh statistics. For full 3D visualization, please use specialized 3D viewing software.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting OBJ to PDF: " + e.getMessage(), e);
        }
    }
    
    private ObjFileData parseObjFile(MultipartFile file) throws IOException {
        ObjFileData data = new ObjFileData();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue;
                
                switch (parts[0]) {
                    case "v":
                        data.vertexCount++;
                        break;
                    case "vt":
                        data.texCoordCount++;
                        break;
                    case "vn":
                        data.normalCount++;
                        break;
                    case "f":
                        data.faceCount++;
                        break;
                    case "g":
                        if (parts.length > 1) {
                            data.groups.add(parts[1]);
                        }
                        break;
                    case "usemtl":
                        if (parts.length > 1) {
                            data.materials.add(parts[1]);
                        }
                        break;
                    case "mtllib":
                        if (parts.length > 1) {
                            data.materialLibrary = parts[1];
                        }
                        break;
                }
            }
        }
        
        return data;
    }
    
    private static class ObjFileData {
        int vertexCount = 0;
        int texCoordCount = 0;
        int normalCount = 0;
        int faceCount = 0;
        List<String> groups = new ArrayList<>();
        List<String> materials = new ArrayList<>();
        String materialLibrary = null;
    }
}
