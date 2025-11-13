package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.xtopdf.xtopdf.entities.*;

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
 * This implementation parses DXF entities and renders them as actual graphics
 * in the PDF using iText's canvas API.
 * 
 * Supported entities: LINE, CIRCLE, ARC, ELLIPSE, POINT, POLYLINE, SOLID/TRACE
 */
@Service
public class DxfToPdfService {
    
    // DXF entity types
    private static final String ENTITY_LINE = "LINE";
    private static final String ENTITY_CIRCLE = "CIRCLE";
    private static final String ENTITY_ARC = "ARC";
    private static final String ENTITY_ELLIPSE = "ELLIPSE";
    private static final String ENTITY_POINT = "POINT";
    private static final String ENTITY_POLYLINE = "POLYLINE";
    private static final String ENTITY_LWPOLYLINE = "LWPOLYLINE";
    private static final String ENTITY_SOLID = "SOLID";
    private static final String ENTITY_TRACE = "TRACE";
    
    // DXF group codes
    private static final int GROUP_CODE_ENTITY_TYPE = 0;
    private static final int GROUP_CODE_LAYER = 8;
    private static final int GROUP_CODE_X_START = 10;
    private static final int GROUP_CODE_Y_START = 20;
    private static final int GROUP_CODE_X_END = 11;
    private static final int GROUP_CODE_Y_END = 21;
    private static final int GROUP_CODE_X2 = 12;
    private static final int GROUP_CODE_Y2 = 22;
    private static final int GROUP_CODE_X3 = 13;
    private static final int GROUP_CODE_Y3 = 23;
    private static final int GROUP_CODE_RADIUS = 40;
    private static final int GROUP_CODE_START_ANGLE = 50;
    private static final int GROUP_CODE_END_ANGLE = 51;
    private static final int GROUP_CODE_RATIO = 40; // Also used for ellipse ratio
    private static final int GROUP_CODE_VERTEX_COUNT = 90;
    
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
            canvas.setFillColor(ColorConstants.LIGHT_GRAY);
            canvas.setLineWidth(1);
            
            // Calculate scale factor to fit drawing on page
            double scale = calculateScale(entities, page.getPageSize().getWidth(), page.getPageSize().getHeight());
            double offsetX = 50; // Left margin
            double offsetY = 50; // Bottom margin
            
            // Render each entity
            for (DxfEntity entity : entities) {
                renderEntity(canvas, entity, scale, offsetX, offsetY);
            }
            
            pdfDocument.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF from DXF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render a single entity on the PDF canvas.
     */
    private void renderEntity(PdfCanvas canvas, DxfEntity entity, double scale, double offsetX, double offsetY) {
        if (entity instanceof LineEntity) {
            LineEntity line = (LineEntity) entity;
            canvas.moveTo(offsetX + line.getX1() * scale, offsetY + line.getY1() * scale);
            canvas.lineTo(offsetX + line.getX2() * scale, offsetY + line.getY2() * scale);
            canvas.stroke();
            
        } else if (entity instanceof CircleEntity) {
            CircleEntity circle = (CircleEntity) entity;
            canvas.circle(offsetX + circle.getCenterX() * scale, offsetY + circle.getCenterY() * scale, 
                         circle.getRadius() * scale);
            canvas.stroke();
            
        } else if (entity instanceof ArcEntity) {
            ArcEntity arc = (ArcEntity) entity;
            double centerX = offsetX + arc.getCenterX() * scale;
            double centerY = offsetY + arc.getCenterY() * scale;
            double radius = arc.getRadius() * scale;
            // Draw arc using iText's arc method (angles in degrees)
            canvas.arc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                      arc.getStartAngle(), arc.getEndAngle() - arc.getStartAngle());
            canvas.stroke();
            
        } else if (entity instanceof PointEntity) {
            PointEntity point = (PointEntity) entity;
            double x = offsetX + point.getX() * scale;
            double y = offsetY + point.getY() * scale;
            double size = 2; // Small cross size
            canvas.moveTo(x - size, y);
            canvas.lineTo(x + size, y);
            canvas.moveTo(x, y - size);
            canvas.lineTo(x, y + size);
            canvas.stroke();
            
        } else if (entity instanceof PolylineEntity) {
            PolylineEntity polyline = (PolylineEntity) entity;
            List<Double> vertices = polyline.getVertices();
            if (vertices.size() >= 4) {
                canvas.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    canvas.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                if (polyline.isClosed()) {
                    canvas.closePath();
                }
                canvas.stroke();
            }
            
        } else if (entity instanceof EllipseEntity) {
            EllipseEntity ellipse = (EllipseEntity) entity;
            double centerX = offsetX + ellipse.getCenterX() * scale;
            double centerY = offsetY + ellipse.getCenterY() * scale;
            double majorRadius = Math.sqrt(ellipse.getMajorAxisX() * ellipse.getMajorAxisX() + 
                                          ellipse.getMajorAxisY() * ellipse.getMajorAxisY()) * scale;
            double minorRadius = majorRadius * ellipse.getRatio();
            // Simplified ellipse rendering as circle for now
            canvas.ellipse(centerX - majorRadius, centerY - minorRadius, 
                          centerX + majorRadius, centerY + minorRadius);
            canvas.stroke();
            
        } else if (entity instanceof SolidEntity) {
            SolidEntity solid = (SolidEntity) entity;
            canvas.moveTo(offsetX + solid.getX1() * scale, offsetY + solid.getY1() * scale);
            canvas.lineTo(offsetX + solid.getX2() * scale, offsetY + solid.getY2() * scale);
            canvas.lineTo(offsetX + solid.getX3() * scale, offsetY + solid.getY3() * scale);
            if (!solid.isTriangle()) {
                canvas.lineTo(offsetX + solid.getX4() * scale, offsetY + solid.getY4() * scale);
            }
            canvas.closePath();
            canvas.fillStroke();
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
            DxfEntity currentEntity = null;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
                if (currentGroupCode == null) {
                    try {
                        currentGroupCode = Integer.parseInt(line);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else {
                    if (currentGroupCode == GROUP_CODE_ENTITY_TYPE) {
                        // Save previous entity if complete
                        if (currentEntity != null) {
                            entities.add(currentEntity);
                        }
                        currentEntityType = line;
                        currentEntity = createEntity(currentEntityType);
                    } else if (currentEntity != null) {
                        parseEntityProperty(currentEntity, currentGroupCode, line);
                    }
                    currentGroupCode = null;
                }
            }
            
            // Add the last entity
            if (currentEntity != null) {
                entities.add(currentEntity);
            }
        }
        
        return entities;
    }
    
    private DxfEntity createEntity(String entityType) {
        switch (entityType) {
            case ENTITY_LINE: return new LineEntity();
            case ENTITY_CIRCLE: return new CircleEntity();
            case ENTITY_ARC: return new ArcEntity();
            case ENTITY_ELLIPSE: return new EllipseEntity();
            case ENTITY_POINT: return new PointEntity();
            case ENTITY_POLYLINE:
            case ENTITY_LWPOLYLINE: return new PolylineEntity();
            case ENTITY_SOLID:
            case ENTITY_TRACE: return new SolidEntity();
            default: return null;
        }
    }
    
    private void parseEntityProperty(DxfEntity entity, int groupCode, String value) {
        try {
            double doubleValue = Double.parseDouble(value);
            
            if (entity instanceof LineEntity) {
                LineEntity line = (LineEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: line.setX1(doubleValue); break;
                    case GROUP_CODE_Y_START: line.setY1(doubleValue); break;
                    case GROUP_CODE_X_END: line.setX2(doubleValue); break;
                    case GROUP_CODE_Y_END: line.setY2(doubleValue); break;
                }
            } else if (entity instanceof CircleEntity) {
                CircleEntity circle = (CircleEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: circle.setCenterX(doubleValue); break;
                    case GROUP_CODE_Y_START: circle.setCenterY(doubleValue); break;
                    case GROUP_CODE_RADIUS: circle.setRadius(doubleValue); break;
                }
            } else if (entity instanceof ArcEntity) {
                ArcEntity arc = (ArcEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: arc.setCenterX(doubleValue); break;
                    case GROUP_CODE_Y_START: arc.setCenterY(doubleValue); break;
                    case GROUP_CODE_RADIUS: arc.setRadius(doubleValue); break;
                    case GROUP_CODE_START_ANGLE: arc.setStartAngle(doubleValue); break;
                    case GROUP_CODE_END_ANGLE: arc.setEndAngle(doubleValue); break;
                }
            } else if (entity instanceof PointEntity) {
                PointEntity point = (PointEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: point.setX(doubleValue); break;
                    case GROUP_CODE_Y_START: point.setY(doubleValue); break;
                }
            } else if (entity instanceof PolylineEntity) {
                PolylineEntity polyline = (PolylineEntity) entity;
                if (groupCode == GROUP_CODE_X_START) {
                    polyline.addVertex(doubleValue, 0); // Will be updated with Y
                } else if (groupCode == GROUP_CODE_Y_START && polyline.getVertexCount() > 0) {
                    List<Double> vertices = polyline.getVertices();
                    vertices.set(vertices.size() - 1, doubleValue);
                }
            } else if (entity instanceof EllipseEntity) {
                EllipseEntity ellipse = (EllipseEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: ellipse.setCenterX(doubleValue); break;
                    case GROUP_CODE_Y_START: ellipse.setCenterY(doubleValue); break;
                    case GROUP_CODE_X_END: ellipse.setMajorAxisX(doubleValue); break;
                    case GROUP_CODE_Y_END: ellipse.setMajorAxisY(doubleValue); break;
                    case GROUP_CODE_RADIUS: ellipse.setRatio(doubleValue); break;
                }
            } else if (entity instanceof SolidEntity) {
                SolidEntity solid = (SolidEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: solid.setX1(doubleValue); break;
                    case GROUP_CODE_Y_START: solid.setY1(doubleValue); break;
                    case GROUP_CODE_X_END: solid.setX2(doubleValue); break;
                    case GROUP_CODE_Y_END: solid.setY2(doubleValue); break;
                    case GROUP_CODE_X2: solid.setX3(doubleValue); break;
                    case GROUP_CODE_Y2: solid.setY3(doubleValue); break;
                    case GROUP_CODE_X3: solid.setX4(doubleValue); break;
                    case GROUP_CODE_Y3: solid.setY4(doubleValue); break;
                }
            }
        } catch (NumberFormatException e) {
            // Skip invalid numeric values
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
                minX = Math.min(minX, Math.min(line.getX1(), line.getX2()));
                minY = Math.min(minY, Math.min(line.getY1(), line.getY2()));
                maxX = Math.max(maxX, Math.max(line.getX1(), line.getX2()));
                maxY = Math.max(maxY, Math.max(line.getY1(), line.getY2()));
            } else if (entity instanceof CircleEntity) {
                CircleEntity circle = (CircleEntity) entity;
                minX = Math.min(minX, circle.getCenterX() - circle.getRadius());
                minY = Math.min(minY, circle.getCenterY() - circle.getRadius());
                maxX = Math.max(maxX, circle.getCenterX() + circle.getRadius());
                maxY = Math.max(maxY, circle.getCenterY() + circle.getRadius());
            } else if (entity instanceof ArcEntity) {
                ArcEntity arc = (ArcEntity) entity;
                minX = Math.min(minX, arc.getCenterX() - arc.getRadius());
                minY = Math.min(minY, arc.getCenterY() - arc.getRadius());
                maxX = Math.max(maxX, arc.getCenterX() + arc.getRadius());
                maxY = Math.max(maxY, arc.getCenterY() + arc.getRadius());
            } else if (entity instanceof PointEntity) {
                PointEntity point = (PointEntity) entity;
                minX = Math.min(minX, point.getX());
                minY = Math.min(minY, point.getY());
                maxX = Math.max(maxX, point.getX());
                maxY = Math.max(maxY, point.getY());
            }
            // Add other entity types for bounding box calculation
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
}
