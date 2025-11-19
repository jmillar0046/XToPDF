package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

/**
 * Service to convert X3D files to PDF.
 * X3D is an XML-based 3D scene format (successor to VRML).
 * This converter parses the X3D file and provides scene statistics.
 */
@Service
public class X3dToPdfService {
    
    public void convertX3dToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            
            // Parse X3D file
            X3dFileData x3dData = parseX3dFile(inputFile);
            
            // Add title
            document.add(new Paragraph("X3D Scene Analysis")
                .setFontSize(18)
                .setMarginBottom(10));
            
            // Add file information
            document.add(new Paragraph("File: " + inputFile.getOriginalFilename()).setFontSize(12));
            document.add(new Paragraph("Format: X3D (Extensible 3D)").setFontSize(12));
            document.add(new Paragraph("Version: " + x3dData.version).setFontSize(12));
            document.add(new Paragraph(""));
            
            // Add scene statistics
            document.add(new Paragraph("Scene Statistics:").setFontSize(14));
            document.add(new Paragraph("Total Nodes: " + x3dData.totalNodes).setFontSize(12));
            document.add(new Paragraph("Shapes: " + x3dData.shapeCount).setFontSize(12));
            document.add(new Paragraph("Transforms: " + x3dData.transformCount).setFontSize(12));
            document.add(new Paragraph("Materials: " + x3dData.materialCount).setFontSize(12));
            document.add(new Paragraph("Geometries: " + x3dData.geometryCount).setFontSize(12));
            
            if (!x3dData.nodeTypes.isEmpty()) {
                document.add(new Paragraph(""));
                document.add(new Paragraph("Node Types Found:").setFontSize(12));
                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(x3dData.nodeTypes.entrySet());
                sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                for (int i = 0; i < Math.min(15, sortedEntries.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedEntries.get(i);
                    document.add(new Paragraph("  • " + entry.getKey() + ": " + entry.getValue()).setFontSize(10));
                }
                if (sortedEntries.size() > 15) {
                    document.add(new Paragraph("  ... and " + (sortedEntries.size() - 15) + " more types").setFontSize(10));
                }
            }
            
            document.add(new Paragraph(""));
            document.add(new Paragraph("Note: This PDF contains scene statistics. For full 3D visualization, use X3D viewers or convert to other 3D formats.").setFontSize(10));
            
            document.close();
        } catch (Exception e) {
            throw new IOException("Error converting X3D to PDF: " + e.getMessage(), e);
        }
    }
    
    private X3dFileData parseX3dFile(MultipartFile file) throws IOException {
        X3dFileData data = new X3dFileData();
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Prevent XXE: disable DTDs and external entities
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(file.getInputStream());
            
            // Get version from X3D root element
            Element root = doc.getDocumentElement();
            if (root.hasAttribute("version")) {
                data.version = root.getAttribute("version");
            }
            
            // Count all nodes recursively
            countNodes(root, data);
            
        } catch (Exception e) {
            throw new IOException("Error parsing X3D file: " + e.getMessage(), e);
        }
        
        return data;
    }
    
    private void countNodes(Node node, X3dFileData data) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String nodeName = node.getNodeName();
            data.totalNodes++;
            data.nodeTypes.put(nodeName, data.nodeTypes.getOrDefault(nodeName, 0) + 1);
            
            // Count specific types
            if (nodeName.equals("Shape")) data.shapeCount++;
            else if (nodeName.equals("Transform")) data.transformCount++;
            else if (nodeName.contains("Material")) data.materialCount++;
            else if (nodeName.endsWith("Geometry") || nodeName.equals("Box") || nodeName.equals("Sphere") || 
                     nodeName.equals("Cone") || nodeName.equals("Cylinder") || nodeName.equals("IndexedFaceSet")) {
                data.geometryCount++;
            }
        }
        
        // Recursively process child nodes
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            countNodes(children.item(i), data);
        }
    }
    
    private static class X3dFileData {
        String version = "Unknown";
        int totalNodes = 0;
        int shapeCount = 0;
        int transformCount = 0;
        int materialCount = 0;
        int geometryCount = 0;
        Map<String, Integer> nodeTypes = new HashMap<>();
    }
}
