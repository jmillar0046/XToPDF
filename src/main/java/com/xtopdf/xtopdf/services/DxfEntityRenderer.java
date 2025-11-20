package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Renderer for DXF entities to PDF using DxfPdfRenderer.
 * Handles recursive rendering for blocks and various entity types.
 */
public class DxfEntityRenderer {
    
    private final Map<String, BlockEntity> blockRegistry;
    
    public DxfEntityRenderer(Map<String, BlockEntity> blockRegistry) {
        this.blockRegistry = blockRegistry;
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
    public void renderEntity(DxfPdfRenderer renderer, DxfEntity entity, double scale, double offsetX, double offsetY,
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
            // Draw arc (angles in degrees)
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
                10.0f // Default text height
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
                renderer.addText(textX, textY, leaderText, 10.0f);
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
            // Render the tolerance value inside the box
            renderer.addText(x + 2, y + height * 0.2, tolerance.getToleranceString(), 8);
            
            
            
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
}
