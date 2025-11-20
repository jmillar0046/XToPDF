package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;

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
    
    private final PdfBackendProvider pdfBackend;
    
    public X3dToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertX3dToPdf(MultipartFile inputFile, File pdfFile) throws IOException {
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            
            // Parse X3D file
            X3dFileData x3dData = parseX3dFile(inputFile);
            
            // Add title
            builder.addParagraph("X3D Scene Analysis\n\n");
            
            // Add file information
            builder.addParagraph("File: " + inputFile.getOriginalFilename());
            builder.addParagraph("Format: X3D (Extensible 3D)");
            builder.addParagraph("Version: " + x3dData.version);
            builder.addParagraph("");
            
            // Add scene statistics
            builder.addParagraph("Scene Statistics:");
            builder.addParagraph("Total Nodes: " + x3dData.totalNodes);
            builder.addParagraph("Shapes: " + x3dData.shapeCount);
            builder.addParagraph("Transforms: " + x3dData.transformCount);
            builder.addParagraph("Materials: " + x3dData.materialCount);
            builder.addParagraph("Geometries: " + x3dData.geometryCount);
            
            if (!x3dData.nodeTypes.isEmpty()) {
                builder.addParagraph("");
                builder.addParagraph("Node Types Found:");
                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(x3dData.nodeTypes.entrySet());
                sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
                for (int i = 0; i < Math.min(15, sortedEntries.size()); i++) {
                    Map.Entry<String, Integer> entry = sortedEntries.get(i);
                    builder.addParagraph("  • " + entry.getKey() + ": " + entry.getValue());
                }
                if (sortedEntries.size() > 15) {
                    builder.addParagraph("  ... and " + (sortedEntries.size() - 15) + " more types");
                }
            }
            
            builder.addParagraph("");
            builder.addParagraph("Note: This PDF contains scene statistics. For full 3D visualization, use X3D viewers or convert to other 3D formats.");
            
            builder.save(pdfFile);
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
