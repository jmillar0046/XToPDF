package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;

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
    
    private final PdfBackendProvider pdfBackend;
    
    @Autowired
    public DxfToPdfService(PdfBackendProvider pdfBackend) {
        this.pdfBackend = pdfBackend;
    }
    
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
    private static final String ENTITY_TEXT = "TEXT";
    private static final String ENTITY_MTEXT = "MTEXT";
    private static final String ENTITY_DIMENSION = "DIMENSION";
    private static final String ENTITY_LEADER = "LEADER";
    private static final String ENTITY_MULTILEADER = "MULTILEADER";
    private static final String ENTITY_TOLERANCE = "TOLERANCE";
    private static final String ENTITY_TABLE = "ACAD_TABLE";
    private static final String ENTITY_BLOCK = "BLOCK";
    private static final String ENTITY_ENDBLK = "ENDBLK";
    private static final String ENTITY_INSERT = "INSERT";
    private static final String ENTITY_ATTDEF = "ATTDEF";
    private static final String ENTITY_ATTRIB = "ATTRIB";
    private static final String ENTITY_XREF = "XREF";
    private static final String ENTITY_WIPEOUT = "WIPEOUT";
    private static final String ENTITY_3DFACE = "3DFACE";
    private static final String ENTITY_3DSOLID = "3DSOLID";
    private static final String ENTITY_POLYFACE_MESH = "POLYLINE"; // POLYFACE variant
    private static final String ENTITY_MESH = "MESH";
    private static final String ENTITY_SURFACE = "SURFACE";
    private static final String ENTITY_BODY = "BODY";
    private static final String ENTITY_REGION = "REGION";
    private static final String ENTITY_VIEWPORT = "VIEWPORT";
    private static final String ENTITY_IMAGE = "IMAGE";
    private static final String ENTITY_PDFUNDERLAY = "PDFUNDERLAY";
    private static final String ENTITY_DGNUNDERLAY = "DGNUNDERLAY";
    private static final String ENTITY_DWFUNDERLAY = "DWFUNDERLAY";
    private static final String ENTITY_OLEFRAME = "OLEFRAME";
    private static final String ENTITY_OLE2FRAME = "OLE2FRAME";
    
    // DXF group codes
    private static final int GROUP_CODE_ENTITY_TYPE = 0;
    private static final int GROUP_CODE_TEXT_VALUE = 1;
    private static final int GROUP_CODE_BLOCK_NAME = 2;
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
    
    // Block registry for storing block definitions (supports recursive blocks)
    private java.util.Map<String, BlockEntity> blockRegistry = new java.util.HashMap<>();
    
    /**
     * Safely parse an integer from user-controlled string input.
     * Prevents numeric cast vulnerabilities by validating the range.
     * 
     * @param value The string value to parse
     * @return The parsed integer value
     * @throws NumberFormatException if the value is not a valid integer or out of range
     */
    private int safeParseInt(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Cannot parse null or empty string");
        }
        
        // First parse as long to check range before casting to int
        long longValue = Long.parseLong(value.trim());
        
        // Validate the value is within int range
        if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
            throw new NumberFormatException("Value out of int range: " + longValue);
        }
        
        return (int) longValue;
    }
    
    /**
     * Safely parse a double from user-controlled string input.
     * Prevents numeric issues by validating the value is finite.
     * 
     * @param value The string value to parse
     * @return The parsed double value
     * @throws NumberFormatException if the value is not a valid double or infinite/NaN
     */
    private double safeParseDouble(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Cannot parse null or empty string");
        }
        
        double doubleValue = Double.parseDouble(value.trim());
        
        // Reject infinite and NaN values for security
        if (!Double.isFinite(doubleValue)) {
            throw new NumberFormatException("Value is not finite: " + value);
        }
        
        return doubleValue;
    }
    
    /**
     * Safely cast a double to an int, preventing user-controlled data vulnerabilities.
     * Validates that the double value is within int range before casting.
     * 
     * @param value The double value to cast to int
     * @return The casted int value
     * @throws NumberFormatException if the value is out of int range or not finite
     */
    private int safeDoubleToInt(double value) throws NumberFormatException {
        // Reject infinite and NaN values
        if (!Double.isFinite(value)) {
            throw new NumberFormatException("Cannot cast non-finite value to int: " + value);
        }
        
        // Check if value is within int range
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new NumberFormatException("Value out of int range: " + value);
        }
        
        return (int) value;
    }
    
    public void convertDxfToPdf(MultipartFile dxfFile, File pdfFile) throws IOException {
        // Parse DXF entities and blocks
        blockRegistry.clear(); // Reset block registry
        List<DxfEntity> entities = parseDxfEntities(dxfFile);
        
        // Create PDF using PDFBox abstraction
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Calculate scale factor to fit drawing on page (A4 = 595x842 points)
            double scale = calculateScale(entities, 595, 842);
            double offsetX = 50; // Left margin
            double offsetY = 50; // Bottom margin
            
            // Create DxfPdfRenderer helper for canvas-like operations
            DxfPdfRenderer renderer = new DxfPdfRenderer(builder);
            
            // Set up drawing parameters
            renderer.setStrokeColor(0, 0, 0); // Black
            renderer.setFillColor(0.827f, 0.827f, 0.827f); // Light gray
            renderer.setLineWidth(1);
            
            // Render each entity (blocks are stored in registry, not rendered directly)
            for (DxfEntity entity : entities) {
                if (!(entity instanceof BlockEntity)) {
                    renderEntity(renderer, entity, scale, offsetX, offsetY, 1.0, 1.0, 0.0);
                }
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from DXF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render a single entity on the PDF using PDFBox renderer with optional transformations.
     * Supports recursive rendering for blocks.
     * 
     * @param renderer DxfPdfRenderer helper
     * @param entity Entity to render
     * @param scale Global scale factor
     * @param offsetX Global X offset
     * @param offsetY Global Y offset
     * @param localScaleX Local X scale (for block insertions)
     * @param localScaleY Local Y scale (for block insertions)
     * @param localRotation Local rotation in degrees (for block insertions)
     */
    private void renderEntity(DxfPdfRenderer renderer, DxfEntity entity, double scale, double offsetX, double offsetY,
                             double localScaleX, double localScaleY, double localRotation) throws IOException {
        if (entity instanceof LineEntity) {
            LineEntity line = (LineEntity) entity;
            renderer.moveTo(offsetX + line.getX1() * scale, offsetY + line.getY1() * scale);
            renderer.lineTo(offsetX + line.getX2() * scale, offsetY + line.getY2() * scale);
            renderer.stroke();
            
        } else if (entity instanceof CircleEntity) {
            CircleEntity circle = (CircleEntity) entity;
            renderer.circle(offsetX + circle.getCenterX() * scale, offsetY + circle.getCenterY() * scale, 
                         circle.getRadius() * scale);
            renderer.stroke();
            
        } else if (entity instanceof ArcEntity) {
            ArcEntity arc = (ArcEntity) entity;
            double centerX = offsetX + arc.getCenterX() * scale;
            double centerY = offsetY + arc.getCenterY() * scale;
            double radius = arc.getRadius() * scale;
            // Draw arc using iText's arc method (angles in degrees)
            renderer.arc(centerX - radius, centerY - radius, centerX + radius, centerY + radius,
                      arc.getStartAngle(), arc.getEndAngle() - arc.getStartAngle());
            renderer.stroke();
            
        } else if (entity instanceof PointEntity) {
            PointEntity point = (PointEntity) entity;
            double x = offsetX + point.getX() * scale;
            double y = offsetY + point.getY() * scale;
            double size = 2; // Small cross size
            renderer.moveTo(x - size, y);
            renderer.lineTo(x + size, y);
            renderer.moveTo(x, y - size);
            renderer.lineTo(x, y + size);
            renderer.stroke();
            
        } else if (entity instanceof PolylineEntity) {
            PolylineEntity polyline = (PolylineEntity) entity;
            List<Double> vertices = polyline.getVertices();
            if (vertices.size() >= 4) {
                renderer.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    renderer.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                if (polyline.isClosed()) {
                    renderer.closePath();
                }
                renderer.stroke();
            }
            
        } else if (entity instanceof EllipseEntity) {
            EllipseEntity ellipse = (EllipseEntity) entity;
            double centerX = offsetX + ellipse.getCenterX() * scale;
            double centerY = offsetY + ellipse.getCenterY() * scale;
            double majorRadius = Math.sqrt(ellipse.getMajorAxisX() * ellipse.getMajorAxisX() + 
                                          ellipse.getMajorAxisY() * ellipse.getMajorAxisY()) * scale;
            double minorRadius = majorRadius * ellipse.getRatio();
            // Simplified ellipse rendering as circle for now
            renderer.ellipse(centerX - majorRadius, centerY - minorRadius, 
                          centerX + majorRadius, centerY + minorRadius);
            renderer.stroke();
            
        } else if (entity instanceof SolidEntity) {
            SolidEntity solid = (SolidEntity) entity;
            renderer.moveTo(offsetX + solid.getX1() * scale, offsetY + solid.getY1() * scale);
            renderer.lineTo(offsetX + solid.getX2() * scale, offsetY + solid.getY2() * scale);
            renderer.lineTo(offsetX + solid.getX3() * scale, offsetY + solid.getY3() * scale);
            if (!solid.isTriangle()) {
                renderer.lineTo(offsetX + solid.getX4() * scale, offsetY + solid.getY4() * scale);
            }
            renderer.closePath();
            renderer.fillStroke();
            
        } else if (entity instanceof TextEntity) {
            TextEntity text = (TextEntity) entity;
            double x = offsetX + text.getX() * scale;
            double y = offsetY + text.getY() * scale;
            // Simplified text rendering - full rotation support would require builder enhancement
            renderer.addText(x, y, text.getText(), (float)(text.getHeight() * scale));
            
        } else if (entity instanceof MTextEntity) {
            MTextEntity mtext = (MTextEntity) entity;
            double x = offsetX + mtext.getX() * scale;
            double y = offsetY + mtext.getY() * scale;
            // Draw box outline
            renderer.rectangle(x, y, mtext.getWidth() * scale, mtext.getHeight() * scale);
            renderer.stroke();
            // Draw text - simplified positioning
            renderer.addText(x + 2, y + mtext.getHeight() * scale - 2, mtext.getText(), (float)(mtext.getHeight() * scale));
            
        } else if (entity instanceof DimensionEntity) {
            DimensionEntity dim = (DimensionEntity) entity;
            double x1 = offsetX + dim.getX1() * scale;
            double y1 = offsetY + dim.getY1() * scale;
            double x2 = offsetX + dim.getX2() * scale;
            double y2 = offsetY + dim.getY2() * scale;
            double textX = offsetX + dim.getTextX() * scale;
            double textY = offsetY + dim.getTextY() * scale;
            
            // Draw dimension line
            renderer.moveTo(x1, y1);
            renderer.lineTo(x2, y2);
            renderer.stroke();
            
            // Draw arrows at endpoints (simplified as small triangles)
            double arrowSize = 3;
            renderer.moveTo(x1, y1);
            renderer.lineTo(x1 + arrowSize, y1 + arrowSize);
            renderer.lineTo(x1 + arrowSize, y1 - arrowSize);
            renderer.fill();
            
            // Draw measurement text
            renderer.addText(
                textX, 
                textY, 
                String.format("%.2f", dim.getMeasurement()), 
                (float)(dim.getTextHeight() * scale)
            );
        } else if (entity instanceof LeaderEntity) {
            LeaderEntity leader = (LeaderEntity) entity;
            List<Double> vertices = leader.getVertices();
            if (vertices.size() >= 4) {
                // Draw leader line
                renderer.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    renderer.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                renderer.stroke();
                
                // Draw arrow at start
                double x1 = offsetX + vertices.get(0) * scale;
                double y1 = offsetY + vertices.get(1) * scale;
                double arrowSize = 3;
                renderer.moveTo(x1, y1);
                renderer.lineTo(x1 + arrowSize, y1 + arrowSize);
                renderer.lineTo(x1 + arrowSize, y1 - arrowSize);
                renderer.fill();
                
                // Draw text
                double textX = offsetX + leader.getTextX() * scale;
                double textY = offsetY + leader.getTextY() * scale;
                String leaderText = leader.getText();
                renderer.showTextAligned(leaderText, textX, textY, 0); // 0 for default alignment
            }
            
        } else if (entity instanceof ToleranceEntity) {
            ToleranceEntity tolerance = (ToleranceEntity) entity;
            double x = offsetX + tolerance.getX() * scale;
            double y = offsetY + tolerance.getY() * scale;
            double height = tolerance.getHeight() * scale;
            
            // Draw tolerance frame (box)
            renderer.rectangle(x, y, height * 4, height);
            renderer.stroke();
            
            // Draw tolerance text
            
            // Text positioning: x + 2, y + height * 0.2);
            // Text content: tolerance.getToleranceString());
            
            
        } else if (entity instanceof TableEntity) {
            TableEntity table = (TableEntity) entity;
            double x = offsetX + table.getX() * scale;
            double y = offsetY + table.getY() * scale;
            double cellWidth = table.getCellWidth() * scale;
            double cellHeight = table.getCellHeight() * scale;
            
            // Draw table grid
            for (int row = 0; row <= table.getRows(); row++) {
                renderer.moveTo(x, y + row * cellHeight);
                renderer.lineTo(x + table.getColumns() * cellWidth, y + row * cellHeight);
            }
            for (int col = 0; col <= table.getColumns(); col++) {
                renderer.moveTo(x + col * cellWidth, y);
                renderer.lineTo(x + col * cellWidth, y + table.getRows() * cellHeight);
            }
            renderer.stroke();
            
            // Draw cell text
            
            List<String> cells = table.getCellValues();
            for (int i = 0; i < cells.size() && i < table.getRows() * table.getColumns(); i++) {
                int row = i / table.getColumns();
                int col = i % table.getColumns();
                double textX = x + col * cellWidth + 2;
                double textY = y + (table.getRows() - row - 1) * cellHeight + cellHeight * 0.3;
                // Simplified text rendering for table cells
                renderer.addText(textX, textY, cells.get(i), 8);
            }
            
            
        } else if (entity instanceof InsertEntity) {
            // INSERT - Render a block with transformations (recursive)
            InsertEntity insert = (InsertEntity) entity;
            BlockEntity block = blockRegistry.get(insert.getBlockName());
            
            if (block != null) {
                // Save canvas state for transformations
                renderer.saveState();
                
                // Calculate transformed position
                double insertX = offsetX + insert.getInsertX() * scale;
                double insertY = offsetY + insert.getInsertY() * scale;
                
                // Note: Full transformation matrix support would require builder enhancement
                // For now, we render blocks without transformations
                
                // Recursively render block contents
                for (DxfEntity blockEntity : block.getEntities()) {
                    renderEntity(renderer, blockEntity, scale * insert.getScaleX(), 
                               insertX - block.getBaseX() * scale, insertY - block.getBaseY() * scale,
                               insert.getScaleX(), insert.getScaleY(), insert.getRotation());
                }
                
                // Restore renderer state
                renderer.restoreState();
            }
            
        } else if (entity instanceof AttributeEntity) {
            // ATTRIB - Render attribute text
            AttributeEntity attr = (AttributeEntity) entity;
            double x = offsetX + attr.getX() * scale * localScaleX;
            double y = offsetY + attr.getY() * scale * localScaleY;
            
            
            // Text positioning: x, y);
            // Text content: attr.getValue());
            
            
        } else if (entity instanceof XRefEntity) {
            // XREF - Render placeholder (external references not automatically loaded for security)
            XRefEntity xref = (XRefEntity) entity;
            double x = offsetX + xref.getInsertX() * scale;
            double y = offsetY + xref.getInsertY() * scale;
            
            // Draw a reference marker
            renderer.rectangle(x, y, 50, 20);
            renderer.stroke();
            
            
            // Text positioning: x + 2, y + 5);
            // Text content: "XREF: " + new java.io.File(xref.getFilePath()).getName());
            
            
        } else if (entity instanceof WipeoutEntity) {
            // WIPEOUT - Render filled white polygon as mask
            WipeoutEntity wipeout = (WipeoutEntity) entity;
            List<Double> vertices = wipeout.getVertices();
            
            if (vertices.size() >= 6) {
                renderer.saveState();
                renderer.setFillColor(1.0f, 1.0f, 1.0f);
                
                renderer.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    renderer.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                renderer.closePath();
                renderer.fill();
                
                renderer.restoreState();
            }
            
        } else if (entity instanceof Face3DEntity) {
            // 3DFACE - Render as filled polygon (project Z coordinate)
            Face3DEntity face3d = (Face3DEntity) entity;
            renderer.saveState();
            renderer.setFillColor(0.827f, 0.827f, 0.827f);
            
            renderer.moveTo(offsetX + face3d.getX1() * scale, offsetY + face3d.getY1() * scale);
            renderer.lineTo(offsetX + face3d.getX2() * scale, offsetY + face3d.getY2() * scale);
            renderer.lineTo(offsetX + face3d.getX3() * scale, offsetY + face3d.getY3() * scale);
            if (!face3d.isTriangle()) {
                renderer.lineTo(offsetX + face3d.getX4() * scale, offsetY + face3d.getY4() * scale);
            }
            renderer.closePath();
            renderer.fillStroke();
            
            renderer.restoreState();
            
        } else if (entity instanceof PolyfaceMeshEntity) {
            // POLYFACE MESH - Render as wireframe (connect vertices)
            PolyfaceMeshEntity mesh = (PolyfaceMeshEntity) entity;
            List<Double> vertices = mesh.getVertices();
            
            if (vertices.size() >= 9) { // At least 3 vertices (x,y,z each)
                renderer.saveState();
                renderer.setStrokeColor(0.66f, 0.66f, 0.66f);
                
                // Draw wireframe by connecting vertices
                for (int i = 0; i < vertices.size() - 3; i += 3) {
                    double x1 = offsetX + vertices.get(i) * scale;
                    double y1 = offsetY + vertices.get(i + 1) * scale;
                    double x2 = offsetX + vertices.get(i + 3) * scale;
                    double y2 = offsetY + vertices.get(i + 4) * scale;
                    
                    renderer.moveTo(x1, y1);
                    renderer.lineTo(x2, y2);
                }
                renderer.stroke();
                
                renderer.restoreState();
            }
            
        } else if (entity instanceof MeshEntity) {
            // MESH - Render as point cloud or wireframe
            MeshEntity mesh = (MeshEntity) entity;
            List<Double> vertices = mesh.getVertices();
            
            if (vertices.size() >= 3) {
                renderer.saveState();
                renderer.setFillColor(0.0f, 0.0f, 1.0f);
                
                // Render as point cloud
                for (int i = 0; i < vertices.size(); i += 3) {
                    double x = offsetX + vertices.get(i) * scale;
                    double y = offsetY + vertices.get(i + 1) * scale;
                    // Draw small circle for each vertex
                    renderer.circle(x, y, 1);
                }
                renderer.fill();
                
                renderer.restoreState();
            }
            
        } else if (entity instanceof Solid3DEntity) {
            // 3DSOLID - Render bounding box placeholder (requires 3D engine)
            Solid3DEntity solid = (Solid3DEntity) entity;
            double x1 = offsetX + solid.getBoundingBoxMinX() * scale;
            double y1 = offsetY + solid.getBoundingBoxMinY() * scale;
            double x2 = offsetX + solid.getBoundingBoxMaxX() * scale;
            double y2 = offsetY + solid.getBoundingBoxMaxY() * scale;
            
            renderer.saveState();
            renderer.setStrokeColor(1.0f, 0.0f, 0.0f);
            renderer.setLineDash(3, 3);
            renderer.rectangle(x1, y1, x2 - x1, y2 - y1);
            renderer.stroke();
            
            // Add label
            
            // Text positioning: x1 + 2, y1 + 2);
            // Text content: "3DSOLID");
            
            
            renderer.restoreState();
            
        } else if (entity instanceof SurfaceEntity) {
            // SURFACE - Render placeholder (requires NURBS renderer)
            SurfaceEntity surface = (SurfaceEntity) entity;
            renderer.saveState();
            renderer.setStrokeColor(0.0f, 1.0f, 0.0f);
            
            // Draw a grid pattern as placeholder
            double gridSize = 50 * scale;
            for (int i = 0; i < 5; i++) {
                double x = offsetX + i * gridSize;
                renderer.moveTo(x, offsetY);
                renderer.lineTo(x, offsetY + 4 * gridSize);
            }
            for (int i = 0; i < 5; i++) {
                double y = offsetY + i * gridSize;
                renderer.moveTo(offsetX, y);
                renderer.lineTo(offsetX + 4 * gridSize, y);
            }
            renderer.stroke();
            
            
            // Text positioning: offsetX + 2, offsetY + 2);
            // Text content: "NURBS SURFACE");
            
            
            renderer.restoreState();
            
        } else if (entity instanceof BodyEntity) {
            // BODY - Render placeholder (requires ACIS kernel)
            renderer.saveState();
            renderer.setStrokeColor(1.0f, 0.65f, 0.0f);
            renderer.rectangle(offsetX, offsetY, 100 * scale, 50 * scale);
            renderer.stroke();
            
            
            // Text positioning: offsetX + 2, offsetY + 2);
            // Text content: "ACIS BODY");
            
            
            renderer.restoreState();
            
        } else if (entity instanceof RegionEntity) {
            // REGION - Render as filled polygon
            RegionEntity region = (RegionEntity) entity;
            List<Double> vertices = region.getVertices();
            
            if (vertices.size() >= 6) {
                renderer.saveState();
                
                if (region.isFilled()) {
                    renderer.setFillColor(200/255f, 220/255f, 255/255f);
                }
                
                renderer.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    renderer.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                renderer.closePath();
                
                if (region.isFilled()) {
                    renderer.fillStroke();
                } else {
                    renderer.stroke();
                }
                
                renderer.restoreState();
            }
            
        } else if (entity instanceof ViewportEntity) {
            // VIEWPORT - Render as clipping rectangle outline
            ViewportEntity viewport = (ViewportEntity) entity;
            double x = offsetX + (viewport.getCenterX() - viewport.getWidth() / 2) * scale;
            double y = offsetY + (viewport.getCenterY() - viewport.getHeight() / 2) * scale;
            double width = viewport.getWidth() * scale;
            double height = viewport.getHeight() * scale;
            
            renderer.saveState();
            renderer.setStrokeColor(1.0f, 0.0f, 1.0f);
            renderer.setLineDash(5, 5);
            renderer.setLineWidth(2);
            renderer.rectangle(x, y, width, height);
            renderer.stroke();
            
            // Add label
            
            // Text positioning: x + 2, y + height - 10);
            // Text content: String.format("VIEWPORT (scale:%.2f)", viewport.getScale()));
            
            
            renderer.restoreState();
            
        } else if (entity instanceof ImageEntity) {
            // IMAGE - Render placeholder for embedded raster image
            ImageEntity image = (ImageEntity) entity;
            double x = offsetX + image.getInsertX() * scale;
            double y = offsetY + image.getInsertY() * scale;
            double width = image.getWidth() * scale;
            double height = image.getHeight() * scale;
            
            renderer.saveState();
            renderer.setStrokeColor(0.0f, 1.0f, 1.0f);
            renderer.setFillColor(240/255f, 248/255f, 255/255f);
            
            // Draw filled rectangle
            renderer.rectangle(x, y, width, height);
            renderer.fillStroke();
            
            // Draw diagonal lines to indicate image
            renderer.moveTo(x, y);
            renderer.lineTo(x + width, y + height);
            renderer.moveTo(x + width, y);
            renderer.lineTo(x, y + height);
            renderer.stroke();
            
            // Add label
            
            // Text positioning: x + 2, y + height / 2);
            String filename = new java.io.File(image.getImagePath()).getName();
            // Text content: "IMAGE: " + (filename.isEmpty() ? "[embedded]" : filename));
            
            
            renderer.restoreState();
            
        } else if (entity instanceof UnderlayEntity) {
            // UNDERLAY - Render placeholder for PDF/DGN/DWF reference
            UnderlayEntity underlay = (UnderlayEntity) entity;
            double x = offsetX + underlay.getInsertX() * scale;
            double y = offsetY + underlay.getInsertY() * scale;
            double width = 150 * scale * underlay.getScaleX();
            double height = 100 * scale * underlay.getScaleY();
            
            renderer.saveState();
            renderer.setStrokeColor(0.0f, 0.0f, 1.0f);
            renderer.setLineDash(3, 3);
            renderer.rectangle(x, y, width, height);
            renderer.stroke();
            
            // Add label
            
            // Text positioning: x + 2, y + height - 10);
            String filename = new java.io.File(underlay.getUnderlayPath()).getName();
            // Text content: underlay.getUnderlayType() + " UNDERLAY: " + filename);
            
            
            renderer.restoreState();
            
        } else if (entity instanceof OleFrameEntity) {
            // OLEFRAME - Render placeholder for linked OLE content
            OleFrameEntity ole = (OleFrameEntity) entity;
            double x = offsetX + ole.getInsertX() * scale;
            double y = offsetY + ole.getInsertY() * scale;
            double width = ole.getWidth() * scale;
            double height = ole.getHeight() * scale;
            
            renderer.saveState();
            renderer.setStrokeColor(0.66f, 0.66f, 0.66f);
            renderer.setFillColor(220/255f, 220/255f, 220/255f);
            
            // Draw filled rectangle
            renderer.rectangle(x, y, width, height);
            renderer.fillStroke();
            
            // Add label
            
            // Text positioning: x + 2, y + height / 2);
            String oleLabel = ole.getOleVersion() == 2 ? "OLE2FRAME" : "OLEFRAME";
            // Text content: oleLabel + (ole.getOleType().isEmpty() ? "" : ": " + ole.getOleType()));
            
            
            renderer.restoreState();
        }
    }
    
    /**
     * Parse DXF entities from the input file.
     * DXF format uses group codes (integers) followed by values.
     */
    /**
     * Parse DXF entities from the input file.
     * DXF format uses group codes (integers) followed by values.
     * Handles BLOCK definitions separately and stores them in blockRegistry.
     */
    private List<DxfEntity> parseDxfEntities(MultipartFile dxfFile) throws IOException {
        List<DxfEntity> entities = new ArrayList<>();
        BlockEntity currentBlock = null;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(dxfFile.getInputStream()))) {
            String line;
            Integer currentGroupCode = null;
            String currentEntityType = null;
            DxfEntity currentEntity = null;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
                if (currentGroupCode == null) {
                    try {
                        currentGroupCode = safeParseInt(line);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                } else {
                    if (currentGroupCode == GROUP_CODE_ENTITY_TYPE) {
                        // Handle ENDBLK - end of block definition
                        if (ENTITY_ENDBLK.equals(line)) {
                            if (currentBlock != null) {
                                // Store block in registry
                                blockRegistry.put(currentBlock.getName(), currentBlock);
                                currentBlock = null;
                            }
                            currentEntity = null;
                            currentEntityType = null;
                        } else {
                            // Save previous entity
                            if (currentEntity != null) {
                                if (currentBlock != null) {
                                    // Entity belongs to current block
                                    currentBlock.addEntity(currentEntity);
                                } else {
                                    // Regular entity
                                    entities.add(currentEntity);
                                }
                            }
                            
                            currentEntityType = line;
                            currentEntity = createEntity(currentEntityType);
                            
                            // Start a new block definition
                            if (ENTITY_BLOCK.equals(line) && currentEntity instanceof BlockEntity) {
                                currentBlock = (BlockEntity) currentEntity;
                                currentEntity = null; // Block itself is not added to entities
                            }
                        }
                    } else if (currentEntity != null) {
                        parseEntityProperty(currentEntity, currentGroupCode, line);
                    } else if (currentBlock != null) {
                        // Parsing block properties (name, base point)
                        if (currentGroupCode == GROUP_CODE_BLOCK_NAME) {
                            currentBlock.setName(line);
                        } else {
                            try {
                                double doubleValue = safeParseDouble(line);
                                if (currentGroupCode == GROUP_CODE_X_START) {
                                    currentBlock.setBaseX(doubleValue);
                                } else if (currentGroupCode == GROUP_CODE_Y_START) {
                                    currentBlock.setBaseY(doubleValue);
                                }
                            } catch (NumberFormatException e) {
                                // Skip invalid values
                            }
                        }
                    }
                    currentGroupCode = null;
                }
            }
            
            // Add the last entity (if not in a block)
            if (currentEntity != null) {
                if (currentBlock != null) {
                    currentBlock.addEntity(currentEntity);
                } else {
                    entities.add(currentEntity);
                }
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
            case ENTITY_TEXT: return new TextEntity();
            case ENTITY_MTEXT: return new MTextEntity();
            case ENTITY_DIMENSION: return new DimensionEntity();
            case ENTITY_LEADER:
            case ENTITY_MULTILEADER: return new LeaderEntity();
            case ENTITY_TOLERANCE: return new ToleranceEntity();
            case ENTITY_TABLE: return new TableEntity();
            case ENTITY_BLOCK: return new BlockEntity();
            case ENTITY_INSERT: return new InsertEntity();
            case ENTITY_ATTDEF:
            case ENTITY_ATTRIB: return new AttributeEntity();
            case ENTITY_XREF: return new XRefEntity();
            case ENTITY_WIPEOUT: return new WipeoutEntity();
            case ENTITY_3DFACE: return new Face3DEntity();
            case ENTITY_3DSOLID: return new Solid3DEntity();
            case ENTITY_MESH: return new MeshEntity();
            case ENTITY_SURFACE: return new SurfaceEntity();
            case ENTITY_BODY: return new BodyEntity();
            case ENTITY_REGION: return new RegionEntity();
            case ENTITY_VIEWPORT: return new ViewportEntity();
            case ENTITY_IMAGE: return new ImageEntity();
            case ENTITY_PDFUNDERLAY:
            case ENTITY_DGNUNDERLAY:
            case ENTITY_DWFUNDERLAY: return new UnderlayEntity();
            case ENTITY_OLEFRAME:
            case ENTITY_OLE2FRAME: return new OleFrameEntity();
            default: return null;
        }
    }
    
    private void parseEntityProperty(DxfEntity entity, int groupCode, String value) {
        // Handle text values first (group code 1)
        if (groupCode == GROUP_CODE_TEXT_VALUE) {
            if (entity instanceof TextEntity) {
                ((TextEntity) entity).setText(value);
            } else if (entity instanceof MTextEntity) {
                ((MTextEntity) entity).setText(value);
            } else if (entity instanceof LeaderEntity) {
                ((LeaderEntity) entity).setText(value);
            } else if (entity instanceof ToleranceEntity) {
                ((ToleranceEntity) entity).setToleranceString(value);
            } else if (entity instanceof TableEntity) {
                ((TableEntity) entity).addCellValue(value);
            }
            return;
        }
        
        try {
            double doubleValue = safeParseDouble(value);
            
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
            } else if (entity instanceof TextEntity) {
                TextEntity text = (TextEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: text.setX(doubleValue); break;
                    case GROUP_CODE_Y_START: text.setY(doubleValue); break;
                    case 40: text.setHeight(doubleValue); break; // Text height
                    case 50: text.setRotationAngle(doubleValue); break; // Rotation angle
                }
            } else if (entity instanceof MTextEntity) {
                MTextEntity mtext = (MTextEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: mtext.setX(doubleValue); break;
                    case GROUP_CODE_Y_START: mtext.setY(doubleValue); break;
                    case 40: mtext.setHeight(doubleValue); break; // Initial text height
                    case 41: mtext.setWidth(doubleValue); break; // Reference column width
                }
            } else if (entity instanceof DimensionEntity) {
                DimensionEntity dim = (DimensionEntity) entity;
                switch (groupCode) {
                    case 70: dim.setDimensionType(safeDoubleToInt(doubleValue)); break;
                    case GROUP_CODE_X_START: dim.setX1(doubleValue); break;
                    case GROUP_CODE_Y_START: dim.setY1(doubleValue); break;
                    case GROUP_CODE_X_END: dim.setX2(doubleValue); break;
                    case GROUP_CODE_Y_END: dim.setY2(doubleValue); break;
                    case 13: dim.setTextX(doubleValue); break;
                    case 23: dim.setTextY(doubleValue); break;
                    case 42: dim.setMeasurement(doubleValue); break;
                }
            } else if (entity instanceof LeaderEntity) {
                LeaderEntity leader = (LeaderEntity) entity;
                if (groupCode == GROUP_CODE_X_START) {
                    leader.addVertex(doubleValue, 0);
                } else if (groupCode == GROUP_CODE_Y_START && leader.getVertexCount() > 0) {
                    List<Double> vertices = leader.getVertices();
                    vertices.set(vertices.size() - 1, doubleValue);
                } else if (groupCode == 13) {
                    leader.setTextX(doubleValue);
                } else if (groupCode == 23) {
                    leader.setTextY(doubleValue);
                }
            } else if (entity instanceof ToleranceEntity) {
                ToleranceEntity tolerance = (ToleranceEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: tolerance.setX(doubleValue); break;
                    case GROUP_CODE_Y_START: tolerance.setY(doubleValue); break;
                    case 40: tolerance.setHeight(doubleValue); break;
                }
            } else if (entity instanceof TableEntity) {
                TableEntity table = (TableEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: table.setX(doubleValue); break;
                    case GROUP_CODE_Y_START: table.setY(doubleValue); break;
                    case 90: table.setRows(safeDoubleToInt(doubleValue)); break;
                    case 91: table.setColumns(safeDoubleToInt(doubleValue)); break;
                    case 40: table.setCellHeight(doubleValue); break;
                    case 41: table.setCellWidth(doubleValue); break;
                }
            } else if (entity instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) entity;
                if (groupCode == GROUP_CODE_BLOCK_NAME) {
                    // Block name is handled as string, not double
                } else {
                    switch (groupCode) {
                        case GROUP_CODE_X_START: block.setBaseX(doubleValue); break;
                        case GROUP_CODE_Y_START: block.setBaseY(doubleValue); break;
                    }
                }
            } else if (entity instanceof InsertEntity) {
                InsertEntity insert = (InsertEntity) entity;
                if (groupCode == GROUP_CODE_BLOCK_NAME) {
                    // Block name handled as string
                } else {
                    switch (groupCode) {
                        case GROUP_CODE_X_START: insert.setInsertX(doubleValue); break;
                        case GROUP_CODE_Y_START: insert.setInsertY(doubleValue); break;
                        case 41: insert.setScaleX(doubleValue); break;
                        case 42: insert.setScaleY(doubleValue); break;
                        case 50: insert.setRotation(doubleValue); break;
                    }
                }
            } else if (entity instanceof AttributeEntity) {
                AttributeEntity attr = (AttributeEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: attr.setX(doubleValue); break;
                    case GROUP_CODE_Y_START: attr.setY(doubleValue); break;
                    case 40: attr.setHeight(doubleValue); break;
                }
            } else if (entity instanceof XRefEntity) {
                XRefEntity xref = (XRefEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: xref.setInsertX(doubleValue); break;
                    case GROUP_CODE_Y_START: xref.setInsertY(doubleValue); break;
                }
            } else if (entity instanceof WipeoutEntity) {
                WipeoutEntity wipeout = (WipeoutEntity) entity;
                if (groupCode == GROUP_CODE_X_START) {
                    wipeout.addVertex(doubleValue, 0);
                } else if (groupCode == GROUP_CODE_Y_START && wipeout.getVertexCount() > 0) {
                    List<Double> vertices = wipeout.getVertices();
                    vertices.set(vertices.size() - 1, doubleValue);
                }
            } else if (entity instanceof Face3DEntity) {
                Face3DEntity face3d = (Face3DEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: face3d.setX1(doubleValue); break;
                    case GROUP_CODE_Y_START: face3d.setY1(doubleValue); break;
                    case 30: face3d.setZ1(doubleValue); break; // Z coordinate
                    case GROUP_CODE_X_END: face3d.setX2(doubleValue); break;
                    case GROUP_CODE_Y_END: face3d.setY2(doubleValue); break;
                    case 31: face3d.setZ2(doubleValue); break;
                    case GROUP_CODE_X2: face3d.setX3(doubleValue); break;
                    case GROUP_CODE_Y2: face3d.setY3(doubleValue); break;
                    case 32: face3d.setZ3(doubleValue); break;
                    case GROUP_CODE_X3: face3d.setX4(doubleValue); break;
                    case GROUP_CODE_Y3: face3d.setY4(doubleValue); break;
                    case 33: face3d.setZ4(doubleValue); break;
                }
            } else if (entity instanceof PolyfaceMeshEntity) {
                PolyfaceMeshEntity mesh = (PolyfaceMeshEntity) entity;
                if (groupCode == GROUP_CODE_X_START) {
                    mesh.addVertex(doubleValue, 0, 0);
                } else if (groupCode == GROUP_CODE_Y_START && mesh.getVertexCount() > 0) {
                    List<Double> vertices = mesh.getVertices();
                    vertices.set(vertices.size() - 2, doubleValue);
                } else if (groupCode == 30 && mesh.getVertexCount() > 0) { // Z coordinate
                    List<Double> vertices = mesh.getVertices();
                    vertices.set(vertices.size() - 1, doubleValue);
                }
            } else if (entity instanceof MeshEntity) {
                MeshEntity mesh = (MeshEntity) entity;
                if (groupCode == GROUP_CODE_X_START) {
                    mesh.addVertex(doubleValue, 0, 0);
                } else if (groupCode == GROUP_CODE_Y_START && mesh.getVertexCount() > 0) {
                    List<Double> vertices = mesh.getVertices();
                    vertices.set(vertices.size() - 2, doubleValue);
                } else if (groupCode == 30 && mesh.getVertexCount() > 0) {
                    List<Double> vertices = mesh.getVertices();
                    vertices.set(vertices.size() - 1, doubleValue);
                } else if (groupCode == 92) {
                    mesh.setSubdivisionLevel(safeDoubleToInt(doubleValue));
                }
            } else if (entity instanceof Solid3DEntity) {
                Solid3DEntity solid = (Solid3DEntity) entity;
                switch (groupCode) {
                    case GROUP_CODE_X_START: solid.setBoundingBoxMinX(doubleValue); break;
                    case GROUP_CODE_Y_START: solid.setBoundingBoxMinY(doubleValue); break;
                    case 30: solid.setBoundingBoxMinZ(doubleValue); break;
                    case GROUP_CODE_X_END: solid.setBoundingBoxMaxX(doubleValue); break;
                    case GROUP_CODE_Y_END: solid.setBoundingBoxMaxY(doubleValue); break;
                    case 31: solid.setBoundingBoxMaxZ(doubleValue); break;
                }
            } else if (entity instanceof SurfaceEntity) {
                SurfaceEntity surface = (SurfaceEntity) entity;
                switch (groupCode) {
                    case 71: surface.setUDegree(safeDoubleToInt(doubleValue)); break;
                    case 72: surface.setVDegree(safeDoubleToInt(doubleValue)); break;
                    case 73: surface.setNumUControlPoints(safeDoubleToInt(doubleValue)); break;
                    case 74: surface.setNumVControlPoints(safeDoubleToInt(doubleValue)); break;
                }
            } else if (entity instanceof BodyEntity) {
                BodyEntity body = (BodyEntity) entity;
                if (groupCode == 70) {
                    body.setVersion(safeDoubleToInt(doubleValue));
                }
            } else if (entity instanceof RegionEntity) {
                RegionEntity region = (RegionEntity) entity;
                if (groupCode == GROUP_CODE_X_START) {
                    region.addVertex(doubleValue, 0);
                } else if (groupCode == GROUP_CODE_Y_START && region.getVertexCount() > 0) {
                    List<Double> vertices = region.getVertices();
                    vertices.set(vertices.size() - 1, doubleValue);
                }
            }
        } catch (NumberFormatException e) {
            // Skip invalid numeric values
        }
        
        // Handle string values (block names, attribute tags, etc.)
        if (groupCode == GROUP_CODE_BLOCK_NAME) {
            if (entity instanceof BlockEntity) {
                ((BlockEntity) entity).setName(value);
            } else if (entity instanceof InsertEntity) {
                ((InsertEntity) entity).setBlockName(value);
            } else if (entity instanceof AttributeEntity) {
                ((AttributeEntity) entity).setTag(value);
            }
        } else if (groupCode == 3) { // Prompt for attributes
            if (entity instanceof AttributeEntity) {
                ((AttributeEntity) entity).setPrompt(value);
            }
        } else if (groupCode == GROUP_CODE_TEXT_VALUE) {
            if (entity instanceof XRefEntity) {
                ((XRefEntity) entity).setFilePath(value);
            } else if (entity instanceof Solid3DEntity) {
                ((Solid3DEntity) entity).setProprietaryData(value);
            } else if (entity instanceof SurfaceEntity) {
                ((SurfaceEntity) entity).setSurfaceData(value);
            } else if (entity instanceof BodyEntity) {
                ((BodyEntity) entity).setAcisData(value);
            }
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
