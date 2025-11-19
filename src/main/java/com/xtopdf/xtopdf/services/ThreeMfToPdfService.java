package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service to convert 3D Manufacturing Format files (3MF) to PDF.
 * 3MF is a ZIP-based XML format for 3D printing.
 * This converter parses the 3MF file and provides model statistics.
 */
@Service
public class ThreeMfToPdfService {
    
    public void convert3mfToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse 3MF file
            ThreeMfFileData mfData = parse3mfFile(inputFile);
            
            // Add title
            document.add(new Paragraph("3MF Model Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: 3MF (3D Manufacturing Format)").setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add model statistics
            document.add(new Paragraph("Model Statistics:").setFontSize(14));
            document.add(new Paragraph("Objects: " + mfData.objectCount).setFontSize(12));
            document.add(new Paragraph("Meshes: " + mfData.meshCount).setFontSize(12));
            document.add(new Paragraph("Total Vertices: " + mfData.vertexCount).setFontSize(12));
            document.add(new Paragraph("Total Triangles: " + mfData.triangleCount).setFontSize(12));
            
            if (!mfData.components.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Components: " + mfData.components.size()).setFontSize(12));
            }
            
            if (!mfData.files.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Package Contents:").setFontSize(12));
                for (String fileName : mfData.files) {
                    document.add(new Paragraph("  • " + fileName).setFontSize(10));
                }
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains model statistics. For 3D printing or visualization, use 3MF-compatible software like 3D Builder or slicing software.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting 3MF to PDF: " + e.getMessage(), e);
        }
    }
    
    private ThreeMfFileData parse3mfFile(MultipartFile file) throws IOException {
        ThreeMfFileData data = new ThreeMfFileData();
        
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                data.files.add(entry.getName());
                
                // Parse 3D/3dmodel.model file (main model file)
                if (entry.getName().endsWith(".model")) {
                    parseModelFile(zis, data);
                }
            }
        } catch (Exception e) {
            throw new IOException("Error parsing 3MF file: " + e.getMessage(), e);
        }
        
        return data;
    }
    
    private void parseModelFile(InputStream inputStream, ThreeMfFileData data) {
        try {
            // Wrap the input stream to prevent the XML parser from closing it
            InputStream nonClosingStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    return inputStream.read();
                }
                
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return inputStream.read(b, off, len);
                }
                
                @Override
                public void close() {
                    // Don't close the underlying stream
                }
            };
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Securely configure to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(nonClosingStream);
            
            // Count objects
            NodeList objects = doc.getElementsByTagName("object");
            data.objectCount = objects.getLength();
            
            // Count meshes and their content
            NodeList meshes = doc.getElementsByTagName("mesh");
            data.meshCount = meshes.getLength();
            
            for (int i = 0; i < meshes.getLength(); i++) {
                Element mesh = (Element) meshes.item(i);
                
                // Count vertices
                NodeList vertices = mesh.getElementsByTagName("vertex");
                data.vertexCount += vertices.getLength();
                
                // Count triangles
                NodeList triangles = mesh.getElementsByTagName("triangle");
                data.triangleCount += triangles.getLength();
            }
            
            // Count components
            NodeList components = doc.getElementsByTagName("component");
            for (int i = 0; i < components.getLength(); i++) {
                Element comp = (Element) components.item(i);
                if (comp.hasAttribute("objectid")) {
                    data.components.add(comp.getAttribute("objectid"));
                }
            }
            
        } catch (Exception e) {
            // If parsing fails, just continue with what we have
        }
    }
    
    private static class ThreeMfFileData {
        int objectCount = 0;
        int meshCount = 0;
        int vertexCount = 0;
        int triangleCount = 0;
        List<String> files = new ArrayList<>();
        Set<String> components = new HashSet<>();
    }
}
