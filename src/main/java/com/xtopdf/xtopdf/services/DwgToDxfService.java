package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.xtopdf.xtopdf.entities.*;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to convert DWG files to DXF format.
 * 
 * This implementation handles a simplified binary DWG format with the following structure:
 * - DWG is a binary file with simple records: type (byte), followed by doubles for coordinates.
 * - Each entity record consists of:
 *   - 1 byte: entity type
 *   - For LINE (type=1): 4 doubles (x1, y1, x2, y2)
 *   - For CIRCLE (type=2): 3 doubles (centerX, centerY, radius)
 *   - For ARC (type=3): 5 doubles (centerX, centerY, radius, startAngle, endAngle)
 *   - For POINT (type=4): 2 doubles (x, y)
 *   - For POLYLINE (type=5): 1 int (numVertices), then numVertices * 2 doubles
 *   - For ELLIPSE (type=6): 7 doubles (centerX, centerY, majorAxisX, majorAxisY, ratio, startParam, endParam)
 *   - For SOLID (type=7): 8 doubles (x1, y1, x2, y2, x3, y3, x4, y4)
 * 
 * The output is a valid ASCII DXF file in R12 format (AC1009) containing HEADER, ENTITIES, and EOF sections.
 */
@Service
public class DwgToDxfService {
    
    // DWG entity type codes
    private static final byte TYPE_LINE = 1;
    private static final byte TYPE_CIRCLE = 2;
    private static final byte TYPE_ARC = 3;
    private static final byte TYPE_POINT = 4;
    private static final byte TYPE_POLYLINE = 5;
    private static final byte TYPE_ELLIPSE = 6;
    private static final byte TYPE_SOLID = 7;
    
    public void convertDwgToDxf(MultipartFile dwgFile, File dxfFile) throws IOException {
        // Parse DWG binary format
        List<DxfEntity> entities = parseDwgFile(dwgFile);
        
        // Write DXF file in R12 format (AC1009)
        writeDxfFile(entities, dxfFile);
    }
    
    /**
     * Parse the simplified binary DWG format.
     */
    private List<DxfEntity> parseDwgFile(MultipartFile dwgFile) throws IOException {
        List<DxfEntity> entities = new ArrayList<>();
        
        try (InputStream is = dwgFile.getInputStream();
             DataInputStream dis = new DataInputStream(is)) {
            
            while (dis.available() > 0) {
                // Read entity type (1 byte)
                byte entityType = dis.readByte();
                
                if (entityType == TYPE_LINE) {
                    // LINE: x1, y1, x2, y2 (4 doubles = 32 bytes)
                    // DXF group codes: 10=x1, 20=y1, 11=x2, 21=y2
                    double x1 = dis.readDouble();
                    double y1 = dis.readDouble();
                    double x2 = dis.readDouble();
                    double y2 = dis.readDouble();
                    entities.add(new LineEntity(x1, y1, x2, y2));
                    
                } else if (entityType == TYPE_CIRCLE) {
                    // CIRCLE: centerX, centerY, radius (3 doubles = 24 bytes)
                    // DXF group codes: 10=centerX, 20=centerY, 40=radius
                    double centerX = dis.readDouble();
                    double centerY = dis.readDouble();
                    double radius = dis.readDouble();
                    entities.add(new CircleEntity(centerX, centerY, radius));
                    
                } else if (entityType == TYPE_ARC) {
                    // ARC: centerX, centerY, radius, startAngle, endAngle (5 doubles = 40 bytes)
                    // DXF group codes: 10=centerX, 20=centerY, 40=radius, 50=startAngle, 51=endAngle
                    double centerX = dis.readDouble();
                    double centerY = dis.readDouble();
                    double radius = dis.readDouble();
                    double startAngle = dis.readDouble();
                    double endAngle = dis.readDouble();
                    entities.add(new ArcEntity(centerX, centerY, radius, startAngle, endAngle));
                    
                } else if (entityType == TYPE_POINT) {
                    // POINT: x, y (2 doubles = 16 bytes)
                    // DXF group codes: 10=x, 20=y
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    entities.add(new PointEntity(x, y));
                    
                } else if (entityType == TYPE_POLYLINE) {
                    // POLYLINE: numVertices (int), then vertex pairs (x, y)
                    // DXF group codes: 90=vertex count, multiple 10/20 pairs
                    int numVertices = dis.readInt();
                    PolylineEntity polyline = new PolylineEntity();
                    for (int i = 0; i < numVertices; i++) {
                        double x = dis.readDouble();
                        double y = dis.readDouble();
                        polyline.addVertex(x, y);
                    }
                    entities.add(polyline);
                    
                } else if (entityType == TYPE_ELLIPSE) {
                    // ELLIPSE: centerX, centerY, majorAxisX, majorAxisY, ratio, startParam, endParam (7 doubles)
                    // DXF group codes: 10/20=center, 11/21=major axis, 40=ratio, 41/42=start/end params
                    double centerX = dis.readDouble();
                    double centerY = dis.readDouble();
                    double majorAxisX = dis.readDouble();
                    double majorAxisY = dis.readDouble();
                    double ratio = dis.readDouble();
                    double startParam = dis.readDouble();
                    double endParam = dis.readDouble();
                    EllipseEntity ellipse = new EllipseEntity(centerX, centerY, majorAxisX, majorAxisY, ratio);
                    ellipse.setStartParam(startParam);
                    ellipse.setEndParam(endParam);
                    entities.add(ellipse);
                    
                } else if (entityType == TYPE_SOLID) {
                    // SOLID: x1, y1, x2, y2, x3, y3, x4, y4 (8 doubles)
                    // DXF group codes: 10/20, 11/21, 12/22, 13/23
                    double x1 = dis.readDouble();
                    double y1 = dis.readDouble();
                    double x2 = dis.readDouble();
                    double y2 = dis.readDouble();
                    double x3 = dis.readDouble();
                    double y3 = dis.readDouble();
                    double x4 = dis.readDouble();
                    double y4 = dis.readDouble();
                    entities.add(new SolidEntity(x1, y1, x2, y2, x3, y3, x4, y4));
                }
            }
        }
        
        return entities;
    }
    
    /**
     * Write a valid ASCII DXF R12 file (AC1009).
     * Format includes: HEADER section, ENTITIES section, and EOF.
     */
    private void writeDxfFile(List<DxfEntity> entities, File dxfFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dxfFile))) {
            // Write HEADER section
            writer.write("0\nSECTION\n2\nHEADER\n9\n$ACADVER\n1\nAC1009\n0\nENDSEC\n");
            
            // Write ENTITIES section
            writer.write("0\nSECTION\n2\nENTITIES\n");
            
            // Write each entity
            for (DxfEntity entity : entities) {
                writeEntity(writer, entity);
            }
            
            // End ENTITIES section
            writer.write("0\nENDSEC\n");
            
            // Write EOF
            writer.write("0\nEOF\n");
        }
    }
    
    private void writeEntity(BufferedWriter writer, DxfEntity entity) throws IOException {
        if (entity instanceof LineEntity) {
            LineEntity line = (LineEntity) entity;
            writer.write("0\nLINE\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", line.getX1(), line.getY1()));
            writer.write(String.format("11\n%.6f\n21\n%.6f\n", line.getX2(), line.getY2()));
            
        } else if (entity instanceof CircleEntity) {
            CircleEntity circle = (CircleEntity) entity;
            writer.write("0\nCIRCLE\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", circle.getCenterX(), circle.getCenterY()));
            writer.write(String.format("40\n%.6f\n", circle.getRadius()));
            
        } else if (entity instanceof ArcEntity) {
            ArcEntity arc = (ArcEntity) entity;
            writer.write("0\nARC\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", arc.getCenterX(), arc.getCenterY()));
            writer.write(String.format("40\n%.6f\n", arc.getRadius()));
            writer.write(String.format("50\n%.6f\n51\n%.6f\n", arc.getStartAngle(), arc.getEndAngle()));
            
        } else if (entity instanceof PointEntity) {
            PointEntity point = (PointEntity) entity;
            writer.write("0\nPOINT\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", point.getX(), point.getY()));
            
        } else if (entity instanceof PolylineEntity) {
            PolylineEntity polyline = (PolylineEntity) entity;
            writer.write("0\nLWPOLYLINE\n8\n0\n");
            writer.write(String.format("90\n%d\n", polyline.getVertexCount()));
            writer.write(String.format("70\n%d\n", polyline.isClosed() ? 1 : 0));
            List<Double> vertices = polyline.getVertices();
            for (int i = 0; i < vertices.size(); i += 2) {
                writer.write(String.format("10\n%.6f\n20\n%.6f\n", vertices.get(i), vertices.get(i + 1)));
            }
            
        } else if (entity instanceof EllipseEntity) {
            EllipseEntity ellipse = (EllipseEntity) entity;
            writer.write("0\nELLIPSE\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", ellipse.getCenterX(), ellipse.getCenterY()));
            writer.write(String.format("11\n%.6f\n21\n%.6f\n", ellipse.getMajorAxisX(), ellipse.getMajorAxisY()));
            writer.write(String.format("40\n%.6f\n", ellipse.getRatio()));
            writer.write(String.format("41\n%.6f\n42\n%.6f\n", ellipse.getStartParam(), ellipse.getEndParam()));
            
        } else if (entity instanceof SolidEntity) {
            SolidEntity solid = (SolidEntity) entity;
            writer.write("0\nSOLID\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", solid.getX1(), solid.getY1()));
            writer.write(String.format("11\n%.6f\n21\n%.6f\n", solid.getX2(), solid.getY2()));
            writer.write(String.format("12\n%.6f\n22\n%.6f\n", solid.getX3(), solid.getY3()));
            writer.write(String.format("13\n%.6f\n23\n%.6f\n", solid.getX4(), solid.getY4()));
        }
    }
}
