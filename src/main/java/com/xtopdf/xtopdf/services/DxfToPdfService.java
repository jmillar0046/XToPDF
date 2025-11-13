package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert DXF (Drawing Exchange Format) files to PDF.
 * 
 * This implementation parses DXF entities (LINE, CIRCLE, etc.) and renders them
 * as actual graphics in the PDF using iText's canvas API.
 */
@Service
public class DxfToPdfService {
    
    // DXF entity types
    private static final String ENTITY_LINE = "LINE";
    private static final String ENTITY_CIRCLE = "CIRCLE";
    
    // DXF group codes
    private static final int GROUP_CODE_ENTITY_TYPE = 0;
    private static final int GROUP_CODE_X_START = 10;
    private static final int GROUP_CODE_Y_START = 20;
    private static final int GROUP_CODE_X_END = 11;
    private static final int GROUP_CODE_Y_END = 21;
    private static final int GROUP_CODE_RADIUS = 40;
    
    public void convertDxfToPdf(MultipartFile dxfFile, File pdfFile) throws IOException {
        // Parse DXF entities
        List<DxfEntity> entities = parseDxfEntities(dxfFile);
        
        // Create a PDF document using iText
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            PdfDocument pdfDocument = new PdfDocument(writer);
            PdfPage page = pdfDocument.addNewPage(PageSize.A4);
            PdfCanvas canvas = new PdfCanvas(page);
            
            // Set up drawing parameters
            canvas.setStrokeColor(ColorConstants.BLACK);
            canvas.setLineWidth(1);
            
            // Calculate scale factor to fit drawing on page
            double scale = calculateScale(entities, page.getPageSize().getWidth(), page.getPageSize().getHeight());
            double offsetX = 50; // Left margin
            double offsetY = 50; // Bottom margin
            
            // Render each entity
            for (DxfEntity entity : entities) {
                if (entity instanceof LineEntity) {
                    LineEntity line = (LineEntity) entity;
                    canvas.moveTo(
                        offsetX + line.x1 * scale,
                        offsetY + line.y1 * scale
                    );
                    canvas.lineTo(
                        offsetX + line.x2 * scale,
                        offsetY + line.y2 * scale
                    );
                    canvas.stroke();
                } else if (entity instanceof CircleEntity) {
                    CircleEntity circle = (CircleEntity) entity;
                    canvas.circle(
                        offsetX + circle.centerX * scale,
                        offsetY + circle.centerY * scale,
                        circle.radius * scale
                    );
                    canvas.stroke();
                }
            }
            
            pdfDocument.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from DXF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse DXF entities from the input file.
     * DXF format uses group codes (integers) followed by values.
     */
    private List<DxfEntity> parseDxfEntities(MultipartFile dxfFile) throws IOException {
        List<DxfEntity> entities = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(dxfFile.getInputStream()))) {
            String line;
            Integer currentGroupCode = null;
            String currentEntityType = null;
            
            // Temporary storage for entity properties
            Double x1 = null, y1 = null, x2 = null, y2 = null, radius = null;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
                if (currentGroupCode == null) {
                    // This line should be a group code
                    try {
                        currentGroupCode = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        // Skip invalid lines
                        continue;
                    }
                } else {
                    // This line is the value for the previous group code
                    if (currentGroupCode == GROUP_CODE_ENTITY_TYPE) {
                        // Save previous entity if complete
                        if (currentEntityType != null) {
                            addEntityIfComplete(entities, currentEntityType, x1, y1, x2, y2, radius);
                            // Reset values
                            x1 = y1 = x2 = y2 = radius = null;
                        }
                        currentEntityType = line;
                    } else if (currentEntityType != null) {
                        // Parse coordinate values
                        try {
                            double value = Double.parseDouble(line);
                            switch (currentGroupCode) {
                                case GROUP_CODE_X_START:
                                    x1 = value;
                                    break;
                                case GROUP_CODE_Y_START:
                                    y1 = value;
                                    break;
                                case GROUP_CODE_X_END:
                                    x2 = value;
                                    break;
                                case GROUP_CODE_Y_END:
                                    y2 = value;
                                    break;
                                case GROUP_CODE_RADIUS:
                                    radius = value;
                                    break;
                            }
                        } catch (NumberFormatException e) {
                            // Skip invalid numeric values
                        }
                    }
                    
                    currentGroupCode = null;
                }
            }
            
            // Add the last entity if present
            if (currentEntityType != null) {
                addEntityIfComplete(entities, currentEntityType, x1, y1, x2, y2, radius);
            }
        }
        
        return entities;
    }
    
    private void addEntityIfComplete(List<DxfEntity> entities, String entityType, 
                                     Double x1, Double y1, Double x2, Double y2, Double radius) {
        if (ENTITY_LINE.equals(entityType) && x1 != null && y1 != null && x2 != null && y2 != null) {
            entities.add(new LineEntity(x1, y1, x2, y2));
        } else if (ENTITY_CIRCLE.equals(entityType) && x1 != null && y1 != null && radius != null) {
            entities.add(new CircleEntity(x1, y1, radius));
        }
    }
    
    private double calculateScale(List<DxfEntity> entities, double pageWidth, double pageHeight) {
        if (entities.isEmpty()) {
            return 1.0;
        }
        
        // Find bounding box
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        for (DxfEntity entity : entities) {
            if (entity instanceof LineEntity) {
                LineEntity line = (LineEntity) entity;
                minX = Math.min(minX, Math.min(line.x1, line.x2));
                minY = Math.min(minY, Math.min(line.y1, line.y2));
                maxX = Math.max(maxX, Math.max(line.x1, line.x2));
                maxY = Math.max(maxY, Math.max(line.y1, line.y2));
            } else if (entity instanceof CircleEntity) {
                CircleEntity circle = (CircleEntity) entity;
                minX = Math.min(minX, circle.centerX - circle.radius);
                minY = Math.min(minY, circle.centerY - circle.radius);
                maxX = Math.max(maxX, circle.centerX + circle.radius);
                maxY = Math.max(maxY, circle.centerY + circle.radius);
            }
        }
        
        double width = maxX - minX;
        double height = maxY - minY;
        
        if (width <= 0 || height <= 0) {
            return 1.0;
        }
        
        // Calculate scale to fit on page with margins
        double availableWidth = pageWidth - 100;
        double availableHeight = pageHeight - 100;
        
        double scaleX = availableWidth / width;
        double scaleY = availableHeight / height;
        
        return Math.min(scaleX, scaleY);
    }
    
    // Inner classes for DXF entities
    private abstract static class DxfEntity {}
    
    private static class LineEntity extends DxfEntity {
        double x1, y1, x2, y2;
        
        LineEntity(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    private static class CircleEntity extends DxfEntity {
        double centerX, centerY, radius;
        
        CircleEntity(double centerX, double centerY, double radius) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
    }
}
