package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Service to convert SVG (Scalable Vector Graphics) files to PDF.
 * 
 * Note: This implementation provides SVG analysis and statistics.
 * For full SVG rendering, consider using external tools like:
 * - Apache Batik (Java SVG toolkit)
 * - Inkscape (command-line conversion)
 * - librsvg (C library with command-line tool)
 * 
 * TODO: For production-grade SVG rendering, integrate with Apache Batik or external renderer.
 */
@Service
public class SvgToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    
    @Autowired
    public SvgToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
    public void convertSvgToPdf(MultipartFile svgFile, File pdfFile) throws IOException {
        try {
            // Parse SVG and extract information
            String svgContent = new String(svgFile.getBytes(), StandardCharsets.UTF_8);
            Document doc = Jsoup.parse(svgContent, "", org.jsoup.parser.Parser.xmlParser());
            
            Element svg = doc.selectFirst("svg");
            if (svg == null) {
                throw new IOException("Invalid SVG file: no <svg> element found");
            }
            
            // Extract SVG properties
            String width = svg.attr("width");
            String height = svg.attr("height");
            String viewBox = svg.attr("viewBox");
            
            // Count elements
            int pathCount = doc.select("path").size();
            int rectCount = doc.select("rect").size();
            int circleCount = doc.select("circle").size();
            int ellipseCount = doc.select("ellipse").size();
            int lineCount = doc.select("line").size();
            int polylineCount = doc.select("polyline").size();
            int polygonCount = doc.select("polygon").size();
            int textCount = doc.select("text").size();
            int imageCount = doc.select("image").size();
            int groupCount = doc.select("g").size();
            
            int totalShapes = pathCount + rectCount + circleCount + ellipseCount + 
                             lineCount + polylineCount + polygonCount;
            
            // Create PDF using abstraction layer
            try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
                builder.addParagraph("SVG Document Analysis\n\n");
                
                builder.addParagraph("File: " + svgFile.getOriginalFilename() + "\n");
                builder.addParagraph("Format: SVG (Scalable Vector Graphics)\n\n");
                
                builder.addParagraph("SVG Properties:\n");
                if (width != null && !width.isEmpty()) {
                    builder.addParagraph("  Width: " + width + "\n");
                }
                if (height != null && !height.isEmpty()) {
                    builder.addParagraph("  Height: " + height + "\n");
                }
                if (viewBox != null && !viewBox.isEmpty()) {
                    builder.addParagraph("  ViewBox: " + viewBox + "\n");
                }
                builder.addParagraph("\n");
                
                builder.addParagraph("Element Statistics:\n");
                builder.addParagraph("  Total Shapes: " + totalShapes + "\n");
                if (pathCount > 0) builder.addParagraph("  Paths: " + pathCount + "\n");
                if (rectCount > 0) builder.addParagraph("  Rectangles: " + rectCount + "\n");
                if (circleCount > 0) builder.addParagraph("  Circles: " + circleCount + "\n");
                if (ellipseCount > 0) builder.addParagraph("  Ellipses: " + ellipseCount + "\n");
                if (lineCount > 0) builder.addParagraph("  Lines: " + lineCount + "\n");
                if (polylineCount > 0) builder.addParagraph("  Polylines: " + polylineCount + "\n");
                if (polygonCount > 0) builder.addParagraph("  Polygons: " + polygonCount + "\n");
                if (textCount > 0) builder.addParagraph("  Text Elements: " + textCount + "\n");
                if (imageCount > 0) builder.addParagraph("  Images: " + imageCount + "\n");
                if (groupCount > 0) builder.addParagraph("  Groups: " + groupCount + "\n");
                
                builder.addParagraph("\nNote: This PDF contains SVG statistics. For visual rendering, use SVG viewers or convert to PDF using Inkscape or Apache Batik.");
                
                builder.save(pdfFile);
            }
        } catch (Exception e) {
            throw new IOException("Error creating PDF from SVG: " + e.getMessage(), e);
        }
    }
}