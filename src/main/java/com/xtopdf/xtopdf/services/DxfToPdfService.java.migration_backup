package com.xtopdf.xtopdf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.entities.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert DXF (Drawing Exchange Format) files to PDF.
 * 
 * This implementation parses DXF entities and renders them as actual graphics
 * in the PDF using Apache PDFBox via the abstraction layer.
 * 
 * Supported entities: LINE, CIRCLE, ARC, ELLIPSE, POINT, POLYLINE, SOLID/TRACE
 * Plus: TEXT, MTEXT, DIMENSION, LEADER, TABLE, BLOCK/INSERT, and 50+ more entity types
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
        
        // Create a PDF document using PDFBox abstraction
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Set up A4 page size (595x842 points)
            builder.setPageSize(595, 842);
            
            // Set up default drawing parameters (black lines, light gray fill)
            builder.setStrokeColor(0, 0, 0); // Black
            builder.setFillColor(0.827f, 0.827f, 0.827f); // Light gray
            builder.setLineWidth(1);
            
            // Calculate scale factor to fit drawing on page
            double pageWidth = 595;
            double pageHeight = 842;
            double scale = calculateScale(entities, pageWidth, pageHeight);
            double offsetX = 50; // Left margin
            double offsetY = 50; // Bottom margin
            
            // Create renderer helper for canvas-like operations
            DxfPdfRenderer renderer = new DxfPdfRenderer(builder);
            
            // Render each entity (blocks are stored in registry, not rendered directly)
            for (DxfEntity entity : entities) {
                if (!(entity instanceof BlockEntity)) {
                    renderEntity(renderer, builder, entity, scale, offsetX, offsetY, 1.0, 1.0, 0.0);
                }
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from DXF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Render a single entity on the PDF using the abstraction layer with optional transformations.
     * Supports recursive rendering for blocks.
     * 
     * @param renderer Canvas-like renderer helper
     * @param builder PDF document builder
     * @param entity Entity to render
     * @param scale Global scale factor
     * @param offsetX Global X offset
     * @param offsetY Global Y offset
     * @param localScaleX Local X scale (for block insertions)
     * @param localScaleY Local Y scale (for block insertions)
     * @param localRotation Local rotation in degrees (for block insertions)
     */
    private void renderEntity(DxfPdfRenderer renderer, PdfDocumentBuilder builder, DxfEntity entity, double scale, double offsetX, double offsetY,
                             double localScaleX, double localScaleY, double localRotation) throws IOException {
        if (entity instanceof LineEntity) {
            LineEntity line = (LineEntity) entity;
            builder.drawLine(
                (float)(offsetX + line.getX1() * scale), (float)(offsetY + line.getY1() * scale),
                (float)(offsetX + line.getX2() * scale), (float)(offsetY + line.getY2() * scale)
            );
            
        } else if (entity instanceof CircleEntity) {
            CircleEntity circle = (CircleEntity) entity;
            builder.drawCircle(
                (float)(offsetX + circle.getCenterX() * scale), 
                (float)(offsetY + circle.getCenterY() * scale),
                (float)(circle.getRadius() * scale)
            );
            
        } else if (entity instanceof ArcEntity) {
            ArcEntity arc = (ArcEntity) entity;
            float centerX = (float)(offsetX + arc.getCenterX() * scale);
            float centerY = (float)(offsetY + arc.getCenterY() * scale);
            float radius = (float)(arc.getRadius() * scale);
            // Draw arc using abstraction (angles in degrees)
            float width = radius * 2;
            float height = radius * 2;
            builder.drawArc(centerX, centerY, width, height,
                          (float)arc.getStartAngle(), (float)(arc.getEndAngle() - arc.getStartAngle()));
            
        } else if (entity instanceof PointEntity) {
            PointEntity point = (PointEntity) entity;
            float x = (float)(offsetX + point.getX() * scale);
            float y = (float)(offsetY + point.getY() * scale);
            float size = 2; // Small cross size
            builder.drawLine(x - size, y, x + size, y);
            builder.drawLine(x, y - size, x, y + size);
            
        } else if (entity instanceof PolylineEntity) {
            PolylineEntity polyline = (PolylineEntity) entity;
            List<Double> vertices = polyline.getVertices();
            if (vertices.size() >= 4) {
                // Build polygon points array
                int numPoints = vertices.size() / 2;
                if (polyline.isClosed()) {
                    numPoints++; // Add closing point
                }
                float[] points = new float[numPoints * 2];
                
                for (int i = 0; i < vertices.size(); i += 2) {
                    points[i] = (float)(offsetX + vertices.get(i) * scale);
                    points[i + 1] = (float)(offsetY + vertices.get(i + 1) * scale);
                }
                
                if (polyline.isClosed() && vertices.size() >= 4) {
                    // Close the path
                    points[points.length - 2] = points[0];
                    points[points.length - 1] = points[1];
                }
                
                builder.drawPolygon(points, false); // Outline only
            }
            
        } else if (entity instanceof EllipseEntity) {
            EllipseEntity ellipse = (EllipseEntity) entity;
            float centerX = (float)(offsetX + ellipse.getCenterX() * scale);
            float centerY = (float)(offsetY + ellipse.getCenterY() * scale);
            double majorRadius = Math.sqrt(ellipse.getMajorAxisX() * ellipse.getMajorAxisX() + 
                                          ellipse.getMajorAxisY() * ellipse.getMajorAxisY()) * scale;
            double minorRadius = majorRadius * ellipse.getRatio();
            builder.drawEllipse(centerX, centerY, (float)(majorRadius * 2), (float)(minorRadius * 2));
            
        } else if (entity instanceof SolidEntity) {
            SolidEntity solid = (SolidEntity) entity;
            float[] points;
            if (solid.isTriangle()) {
                points = new float[6];
                points[0] = (float)(offsetX + solid.getX1() * scale);
                points[1] = (float)(offsetY + solid.getY1() * scale);
                points[2] = (float)(offsetX + solid.getX2() * scale);
                points[3] = (float)(offsetY + solid.getY2() * scale);
                points[4] = (float)(offsetX + solid.getX3() * scale);
                points[5] = (float)(offsetY + solid.getY3() * scale);
            } else {
                points = new float[8];
                points[0] = (float)(offsetX + solid.getX1() * scale);
                points[1] = (float)(offsetY + solid.getY1() * scale);
                points[2] = (float)(offsetX + solid.getX2() * scale);
                points[3] = (float)(offsetY + solid.getY2() * scale);
                points[4] = (float)(offsetX + solid.getX3() * scale);
                points[5] = (float)(offsetY + solid.getY3() * scale);
                points[6] = (float)(offsetX + solid.getX4() * scale);
                points[7] = (float)(offsetY + solid.getY4() * scale);
            }
            builder.drawPolygon(points, true); // Filled polygon
            
        } else if (entity instanceof TextEntity) {
            TextEntity text = (TextEntity) entity;
            double x = offsetX + text.getX() * scale;
            double y = offsetY + text.getY() * scale;
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), (float)(text.getHeight() * scale));
            canvas.moveText(x, y);
            if (text.getRotationAngle() != 0) {
                double radians = Math.toRadians(text.getRotationAngle());
                canvas.setTextMatrix((float)Math.cos(radians), (float)Math.sin(radians),
                                    (float)-Math.sin(radians), (float)Math.cos(radians), (float)x, (float)y);
            }
            canvas.showText(text.getText());
            canvas.endText();
            
        } else if (entity instanceof MTextEntity) {
            MTextEntity mtext = (MTextEntity) entity;
            double x = offsetX + mtext.getX() * scale;
            double y = offsetY + mtext.getY() * scale;
            // Draw box outline
            canvas.rectangle(x, y, mtext.getWidth() * scale, mtext.getHeight() * scale);
            canvas.stroke();
            // Draw text
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), (float)(mtext.getHeight() * scale));
            canvas.moveText(x + 2, y + mtext.getHeight() * scale - 2);
            canvas.showText(mtext.getText());
            canvas.endText();
            
        } else if (entity instanceof DimensionEntity) {
            DimensionEntity dim = (DimensionEntity) entity;
            double x1 = offsetX + dim.getX1() * scale;
            double y1 = offsetY + dim.getY1() * scale;
            double x2 = offsetX + dim.getX2() * scale;
            double y2 = offsetY + dim.getY2() * scale;
            double textX = offsetX + dim.getTextX() * scale;
            double textY = offsetY + dim.getTextY() * scale;
            
            // Draw dimension line
            canvas.moveTo(x1, y1);
            canvas.lineTo(x2, y2);
            canvas.stroke();
            
            // Draw arrows at endpoints (simplified as small triangles)
            double arrowSize = 3;
            canvas.moveTo(x1, y1);
            canvas.lineTo(x1 + arrowSize, y1 + arrowSize);
            canvas.lineTo(x1 + arrowSize, y1 - arrowSize);
            canvas.fill();
            
            // Draw measurement text
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(textX, textY);
            canvas.showText(String.format("%.2f", dim.getMeasurement()));
            canvas.endText();
            
        } else if (entity instanceof LeaderEntity) {
            LeaderEntity leader = (LeaderEntity) entity;
            List<Double> vertices = leader.getVertices();
            if (vertices.size() >= 4) {
                // Draw leader line
                canvas.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    canvas.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                canvas.stroke();
                
                // Draw arrow at start
                double x1 = offsetX + vertices.get(0) * scale;
                double y1 = offsetY + vertices.get(1) * scale;
                double arrowSize = 3;
                canvas.moveTo(x1, y1);
                canvas.lineTo(x1 + arrowSize, y1 + arrowSize);
                canvas.lineTo(x1 + arrowSize, y1 - arrowSize);
                canvas.fill();
                
                // Draw text
                canvas.beginText();
                canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                    com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
                canvas.moveText(offsetX + leader.getTextX() * scale, offsetY + leader.getTextY() * scale);
                canvas.showText(leader.getText());
                canvas.endText();
            }
            
        } else if (entity instanceof ToleranceEntity) {
            ToleranceEntity tolerance = (ToleranceEntity) entity;
            double x = offsetX + tolerance.getX() * scale;
            double y = offsetY + tolerance.getY() * scale;
            double height = tolerance.getHeight() * scale;
            
            // Draw tolerance frame (box)
            canvas.rectangle(x, y, height * 4, height);
            canvas.stroke();
            
            // Draw tolerance text
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), (float)height * 0.8f);
            canvas.moveText(x + 2, y + height * 0.2);
            canvas.showText(tolerance.getToleranceString());
            canvas.endText();
            
        } else if (entity instanceof TableEntity) {
            TableEntity table = (TableEntity) entity;
            double x = offsetX + table.getX() * scale;
            double y = offsetY + table.getY() * scale;
            double cellWidth = table.getCellWidth() * scale;
            double cellHeight = table.getCellHeight() * scale;
            
            // Draw table grid
            for (int row = 0; row <= table.getRows(); row++) {
                canvas.moveTo(x, y + row * cellHeight);
                canvas.lineTo(x + table.getColumns() * cellWidth, y + row * cellHeight);
            }
            for (int col = 0; col <= table.getColumns(); col++) {
                canvas.moveTo(x + col * cellWidth, y);
                canvas.lineTo(x + col * cellWidth, y + table.getRows() * cellHeight);
            }
            canvas.stroke();
            
            // Draw cell text
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), (float)(cellHeight * 0.6));
            List<String> cells = table.getCellValues();
            for (int i = 0; i < cells.size() && i < table.getRows() * table.getColumns(); i++) {
                int row = i / table.getColumns();
                int col = i % table.getColumns();
                canvas.moveText(x + col * cellWidth + 2, 
                              y + (table.getRows() - row - 1) * cellHeight + cellHeight * 0.3);
                canvas.showText(cells.get(i));
            }
            canvas.endText();
            
        } else if (entity instanceof InsertEntity) {
            // INSERT - Render a block with transformations (recursive)
            InsertEntity insert = (InsertEntity) entity;
            BlockEntity block = blockRegistry.get(insert.getBlockName());
            
            if (block != null) {
                // Save canvas state for transformations
                canvas.saveState();
                
                // Calculate transformed position
                double insertX = offsetX + insert.getInsertX() * scale;
                double insertY = offsetY + insert.getInsertY() * scale;
                
                // Apply transformations: translate, rotate, scale
                double radians = Math.toRadians(insert.getRotation());
                canvas.concatMatrix(
                    Math.cos(radians) * insert.getScaleX(), Math.sin(radians) * insert.getScaleX(),
                    -Math.sin(radians) * insert.getScaleY(), Math.cos(radians) * insert.getScaleY(),
                    insertX, insertY
                );
                
                // Recursively render block contents
                for (DxfEntity blockEntity : block.getEntities()) {
                    renderEntity(canvas, blockEntity, scale * insert.getScaleX(), 
                               -block.getBaseX() * scale, -block.getBaseY() * scale,
                               insert.getScaleX(), insert.getScaleY(), insert.getRotation());
                }
                
                // Restore canvas state
                canvas.restoreState();
            }
            
        } else if (entity instanceof AttributeEntity) {
            // ATTRIB - Render attribute text
            AttributeEntity attr = (AttributeEntity) entity;
            double x = offsetX + attr.getX() * scale * localScaleX;
            double y = offsetY + attr.getY() * scale * localScaleY;
            
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), (float)(attr.getHeight() * scale));
            canvas.moveText(x, y);
            canvas.showText(attr.getValue());
            canvas.endText();
            
        } else if (entity instanceof XRefEntity) {
            // XREF - Render placeholder (external references not automatically loaded for security)
            XRefEntity xref = (XRefEntity) entity;
            double x = offsetX + xref.getInsertX() * scale;
            double y = offsetY + xref.getInsertY() * scale;
            
            // Draw a reference marker
            canvas.rectangle(x, y, 50, 20);
            canvas.stroke();
            
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(x + 2, y + 5);
            canvas.showText("XREF: " + new java.io.File(xref.getFilePath()).getName());
            canvas.endText();
            
        } else if (entity instanceof WipeoutEntity) {
            // WIPEOUT - Render filled white polygon as mask
            WipeoutEntity wipeout = (WipeoutEntity) entity;
            List<Double> vertices = wipeout.getVertices();
            
            if (vertices.size() >= 6) {
                canvas.saveState();
                canvas.setFillColor(ColorConstants.WHITE);
                
                canvas.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    canvas.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                canvas.closePath();
                canvas.fill();
                
                canvas.restoreState();
            }
            
        } else if (entity instanceof Face3DEntity) {
            // 3DFACE - Render as filled polygon (project Z coordinate)
            Face3DEntity face3d = (Face3DEntity) entity;
            canvas.saveState();
            canvas.setFillColor(ColorConstants.LIGHT_GRAY);
            
            canvas.moveTo(offsetX + face3d.getX1() * scale, offsetY + face3d.getY1() * scale);
            canvas.lineTo(offsetX + face3d.getX2() * scale, offsetY + face3d.getY2() * scale);
            canvas.lineTo(offsetX + face3d.getX3() * scale, offsetY + face3d.getY3() * scale);
            if (!face3d.isTriangle()) {
                canvas.lineTo(offsetX + face3d.getX4() * scale, offsetY + face3d.getY4() * scale);
            }
            canvas.closePath();
            canvas.fillStroke();
            
            canvas.restoreState();
            
        } else if (entity instanceof PolyfaceMeshEntity) {
            // POLYFACE MESH - Render as wireframe (connect vertices)
            PolyfaceMeshEntity mesh = (PolyfaceMeshEntity) entity;
            List<Double> vertices = mesh.getVertices();
            
            if (vertices.size() >= 9) { // At least 3 vertices (x,y,z each)
                canvas.saveState();
                canvas.setStrokeColor(ColorConstants.DARK_GRAY);
                
                // Draw wireframe by connecting vertices
                for (int i = 0; i < vertices.size() - 3; i += 3) {
                    double x1 = offsetX + vertices.get(i) * scale;
                    double y1 = offsetY + vertices.get(i + 1) * scale;
                    double x2 = offsetX + vertices.get(i + 3) * scale;
                    double y2 = offsetY + vertices.get(i + 4) * scale;
                    
                    canvas.moveTo(x1, y1);
                    canvas.lineTo(x2, y2);
                }
                canvas.stroke();
                
                canvas.restoreState();
            }
            
        } else if (entity instanceof MeshEntity) {
            // MESH - Render as point cloud or wireframe
            MeshEntity mesh = (MeshEntity) entity;
            List<Double> vertices = mesh.getVertices();
            
            if (vertices.size() >= 3) {
                canvas.saveState();
                canvas.setFillColor(ColorConstants.BLUE);
                
                // Render as point cloud
                for (int i = 0; i < vertices.size(); i += 3) {
                    double x = offsetX + vertices.get(i) * scale;
                    double y = offsetY + vertices.get(i + 1) * scale;
                    // Draw small circle for each vertex
                    canvas.circle(x, y, 1);
                }
                canvas.fill();
                
                canvas.restoreState();
            }
            
        } else if (entity instanceof Solid3DEntity) {
            // 3DSOLID - Render bounding box placeholder (requires 3D engine)
            Solid3DEntity solid = (Solid3DEntity) entity;
            double x1 = offsetX + solid.getBoundingBoxMinX() * scale;
            double y1 = offsetY + solid.getBoundingBoxMinY() * scale;
            double x2 = offsetX + solid.getBoundingBoxMaxX() * scale;
            double y2 = offsetY + solid.getBoundingBoxMaxY() * scale;
            
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.RED);
            canvas.setLineDash(3, 3);
            canvas.rectangle(x1, y1, x2 - x1, y2 - y1);
            canvas.stroke();
            
            // Add label
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(x1 + 2, y1 + 2);
            canvas.showText("3DSOLID");
            canvas.endText();
            
            canvas.restoreState();
            
        } else if (entity instanceof SurfaceEntity) {
            // SURFACE - Render placeholder (requires NURBS renderer)
            SurfaceEntity surface = (SurfaceEntity) entity;
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.GREEN);
            
            // Draw a grid pattern as placeholder
            double gridSize = 50 * scale;
            for (int i = 0; i < 5; i++) {
                double x = offsetX + i * gridSize;
                canvas.moveTo(x, offsetY);
                canvas.lineTo(x, offsetY + 4 * gridSize);
            }
            for (int i = 0; i < 5; i++) {
                double y = offsetY + i * gridSize;
                canvas.moveTo(offsetX, y);
                canvas.lineTo(offsetX + 4 * gridSize, y);
            }
            canvas.stroke();
            
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(offsetX + 2, offsetY + 2);
            canvas.showText("NURBS SURFACE");
            canvas.endText();
            
            canvas.restoreState();
            
        } else if (entity instanceof BodyEntity) {
            // BODY - Render placeholder (requires ACIS kernel)
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.ORANGE);
            canvas.rectangle(offsetX, offsetY, 100 * scale, 50 * scale);
            canvas.stroke();
            
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(offsetX + 2, offsetY + 2);
            canvas.showText("ACIS BODY");
            canvas.endText();
            
            canvas.restoreState();
            
        } else if (entity instanceof RegionEntity) {
            // REGION - Render as filled polygon
            RegionEntity region = (RegionEntity) entity;
            List<Double> vertices = region.getVertices();
            
            if (vertices.size() >= 6) {
                canvas.saveState();
                
                if (region.isFilled()) {
                    canvas.setFillColor(new com.itextpdf.kernel.colors.DeviceRgb(200, 220, 255));
                }
                
                canvas.moveTo(offsetX + vertices.get(0) * scale, offsetY + vertices.get(1) * scale);
                for (int i = 2; i < vertices.size(); i += 2) {
                    canvas.lineTo(offsetX + vertices.get(i) * scale, offsetY + vertices.get(i + 1) * scale);
                }
                canvas.closePath();
                
                if (region.isFilled()) {
                    canvas.fillStroke();
                } else {
                    canvas.stroke();
                }
                
                canvas.restoreState();
            }
            
        } else if (entity instanceof ViewportEntity) {
            // VIEWPORT - Render as clipping rectangle outline
            ViewportEntity viewport = (ViewportEntity) entity;
            double x = offsetX + (viewport.getCenterX() - viewport.getWidth() / 2) * scale;
            double y = offsetY + (viewport.getCenterY() - viewport.getHeight() / 2) * scale;
            double width = viewport.getWidth() * scale;
            double height = viewport.getHeight() * scale;
            
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.MAGENTA);
            canvas.setLineDash(5, 5);
            canvas.setLineWidth(2);
            canvas.rectangle(x, y, width, height);
            canvas.stroke();
            
            // Add label
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(x + 2, y + height - 10);
            canvas.showText(String.format("VIEWPORT (scale:%.2f)", viewport.getScale()));
            canvas.endText();
            
            canvas.restoreState();
            
        } else if (entity instanceof ImageEntity) {
            // IMAGE - Render placeholder for embedded raster image
            ImageEntity image = (ImageEntity) entity;
            double x = offsetX + image.getInsertX() * scale;
            double y = offsetY + image.getInsertY() * scale;
            double width = image.getWidth() * scale;
            double height = image.getHeight() * scale;
            
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.CYAN);
            canvas.setFillColor(new com.itextpdf.kernel.colors.DeviceRgb(240, 248, 255));
            
            // Draw filled rectangle
            canvas.rectangle(x, y, width, height);
            canvas.fillStroke();
            
            // Draw diagonal lines to indicate image
            canvas.moveTo(x, y);
            canvas.lineTo(x + width, y + height);
            canvas.moveTo(x + width, y);
            canvas.lineTo(x, y + height);
            canvas.stroke();
            
            // Add label
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(x + 2, y + height / 2);
            String filename = new java.io.File(image.getImagePath()).getName();
            canvas.showText("IMAGE: " + (filename.isEmpty() ? "[embedded]" : filename));
            canvas.endText();
            
            canvas.restoreState();
            
        } else if (entity instanceof UnderlayEntity) {
            // UNDERLAY - Render placeholder for PDF/DGN/DWF reference
            UnderlayEntity underlay = (UnderlayEntity) entity;
            double x = offsetX + underlay.getInsertX() * scale;
            double y = offsetY + underlay.getInsertY() * scale;
            double width = 150 * scale * underlay.getScaleX();
            double height = 100 * scale * underlay.getScaleY();
            
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.BLUE);
            canvas.setLineDash(3, 3);
            canvas.rectangle(x, y, width, height);
            canvas.stroke();
            
            // Add label
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(x + 2, y + height - 10);
            String filename = new java.io.File(underlay.getUnderlayPath()).getName();
            canvas.showText(underlay.getUnderlayType() + " UNDERLAY: " + filename);
            canvas.endText();
            
            canvas.restoreState();
            
        } else if (entity instanceof OleFrameEntity) {
            // OLEFRAME - Render placeholder for linked OLE content
            OleFrameEntity ole = (OleFrameEntity) entity;
            double x = offsetX + ole.getInsertX() * scale;
            double y = offsetY + ole.getInsertY() * scale;
            double width = ole.getWidth() * scale;
            double height = ole.getHeight() * scale;
            
            canvas.saveState();
            canvas.setStrokeColor(ColorConstants.DARK_GRAY);
            canvas.setFillColor(new com.itextpdf.kernel.colors.DeviceRgb(220, 220, 220));
            
            // Draw filled rectangle
            canvas.rectangle(x, y, width, height);
            canvas.fillStroke();
            
            // Add label
            canvas.beginText();
            canvas.setFontAndSize(com.itextpdf.kernel.font.PdfFontFactory.createFont(
                com.itextpdf.io.font.constants.StandardFonts.HELVETICA), 8);
            canvas.moveText(x + 2, y + height / 2);
            String oleLabel = ole.getOleVersion() == 2 ? "OLE2FRAME" : "OLEFRAME";
            canvas.showText(oleLabel + (ole.getOleType().isEmpty() ? "" : ": " + ole.getOleType()));
            canvas.endText();
            
            canvas.restoreState();
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
