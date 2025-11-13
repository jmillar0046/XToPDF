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
    private static final byte TYPE_TEXT = 8;
    private static final byte TYPE_MTEXT = 9;
    private static final byte TYPE_DIMENSION = 10;
    private static final byte TYPE_LEADER = 11;
    private static final byte TYPE_TOLERANCE = 12;
    private static final byte TYPE_TABLE = 13;
    private static final byte TYPE_BLOCK = 14;
    private static final byte TYPE_INSERT = 15;
    private static final byte TYPE_ATTRIB = 16;
    private static final byte TYPE_XREF = 17;
    private static final byte TYPE_WIPEOUT = 18;
    private static final byte TYPE_3DFACE = 19;
    private static final byte TYPE_POLYFACE_MESH = 20;
    private static final byte TYPE_MESH = 21;
    private static final byte TYPE_3DSOLID = 22;
    private static final byte TYPE_SURFACE = 23;
    private static final byte TYPE_BODY = 24;
    private static final byte TYPE_REGION = 25;
    private static final byte TYPE_VIEWPORT = 26;
    private static final byte TYPE_IMAGE = 27;
    private static final byte TYPE_UNDERLAY = 28;
    private static final byte TYPE_OLEFRAME = 29;
    
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
                    
                } else if (entityType == TYPE_TEXT) {
                    // TEXT: x, y, height, textLength, text (3 doubles + text)
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    double height = dis.readDouble();
                    int textLength = dis.readInt();
                    byte[] textBytes = new byte[textLength];
                    dis.readFully(textBytes);
                    String text = new String(textBytes, "UTF-8");
                    entities.add(new TextEntity(x, y, height, text));
                    
                } else if (entityType == TYPE_MTEXT) {
                    // MTEXT: x, y, width, height, textLength, text (4 doubles + text)
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    double width = dis.readDouble();
                    double height = dis.readDouble();
                    int textLength = dis.readInt();
                    byte[] textBytes = new byte[textLength];
                    dis.readFully(textBytes);
                    String text = new String(textBytes, "UTF-8");
                    entities.add(new MTextEntity(x, y, width, height, text));
                    
                } else if (entityType == TYPE_DIMENSION) {
                    // DIMENSION: dimType (byte), x1, y1, x2, y2, textX, textY, measurement (7 doubles)
                    byte dimType = dis.readByte();
                    double x1 = dis.readDouble();
                    double y1 = dis.readDouble();
                    double x2 = dis.readDouble();
                    double y2 = dis.readDouble();
                    double textX = dis.readDouble();
                    double textY = dis.readDouble();
                    double measurement = dis.readDouble();
                    entities.add(new DimensionEntity(dimType, x1, y1, x2, y2, textX, textY, measurement));
                    
                } else if (entityType == TYPE_LEADER) {
                    // LEADER: numVertices (int), vertices, textX, textY, textLength, text
                    int numVertices = dis.readInt();
                    LeaderEntity leader = new LeaderEntity();
                    for (int i = 0; i < numVertices; i++) {
                        double x = dis.readDouble();
                        double y = dis.readDouble();
                        leader.addVertex(x, y);
                    }
                    leader.setTextX(dis.readDouble());
                    leader.setTextY(dis.readDouble());
                    int textLength = dis.readInt();
                    byte[] textBytes = new byte[textLength];
                    dis.readFully(textBytes);
                    leader.setText(new String(textBytes, "UTF-8"));
                    entities.add(leader);
                    
                } else if (entityType == TYPE_TOLERANCE) {
                    // TOLERANCE: x, y, height, textLength, text (3 doubles + text)
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    double height = dis.readDouble();
                    int textLength = dis.readInt();
                    byte[] textBytes = new byte[textLength];
                    dis.readFully(textBytes);
                    String text = new String(textBytes, "UTF-8");
                    entities.add(new ToleranceEntity(x, y, height, text));
                    
                } else if (entityType == TYPE_TABLE) {
                    // TABLE: x, y, rows (int), cols (int), cellHeight, cellWidth, then cell texts
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    int rows = dis.readInt();
                    int columns = dis.readInt();
                    double cellHeight = dis.readDouble();
                    double cellWidth = dis.readDouble();
                    TableEntity table = new TableEntity(x, y, rows, columns, cellHeight, cellWidth);
                    int cellCount = rows * columns;
                    for (int i = 0; i < cellCount; i++) {
                        int cellTextLength = dis.readInt();
                        byte[] cellTextBytes = new byte[cellTextLength];
                        dis.readFully(cellTextBytes);
                        table.addCellValue(new String(cellTextBytes, "UTF-8"));
                    }
                    entities.add(table);
                    
                } else if (entityType == TYPE_BLOCK) {
                    // BLOCK: nameLength, name, baseX, baseY, numEntities, entities...
                    int nameLength = dis.readInt();
                    byte[] nameBytes = new byte[nameLength];
                    dis.readFully(nameBytes);
                    String name = new String(nameBytes, "UTF-8");
                    double baseX = dis.readDouble();
                    double baseY = dis.readDouble();
                    int numEntities = dis.readInt();
                    
                    BlockEntity block = new BlockEntity(name);
                    block.setBaseX(baseX);
                    block.setBaseY(baseY);
                    
                    // Recursively parse block entities
                    for (int i = 0; i < numEntities; i++) {
                        byte blockEntityType = dis.readByte();
                        // Note: In a full implementation, would recursively parse entities here
                        // For simplicity, this shows the structure
                    }
                    entities.add(block);
                    
                } else if (entityType == TYPE_INSERT) {
                    // INSERT: nameLength, name, x, y, scaleX, scaleY, rotation
                    int nameLength = dis.readInt();
                    byte[] nameBytes = new byte[nameLength];
                    dis.readFully(nameBytes);
                    String blockName = new String(nameBytes, "UTF-8");
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    double scaleX = dis.readDouble();
                    double scaleY = dis.readDouble();
                    double rotation = dis.readDouble();
                    
                    InsertEntity insert = new InsertEntity(blockName, x, y);
                    insert.setScaleX(scaleX);
                    insert.setScaleY(scaleY);
                    insert.setRotation(rotation);
                    entities.add(insert);
                    
                } else if (entityType == TYPE_ATTRIB) {
                    // ATTRIB: tagLength, tag, promptLength, prompt, valueLength, value, x, y, height
                    int tagLength = dis.readInt();
                    byte[] tagBytes = new byte[tagLength];
                    dis.readFully(tagBytes);
                    String tag = new String(tagBytes, "UTF-8");
                    
                    int promptLength = dis.readInt();
                    byte[] promptBytes = new byte[promptLength];
                    dis.readFully(promptBytes);
                    String prompt = new String(promptBytes, "UTF-8");
                    
                    int valueLength = dis.readInt();
                    byte[] valueBytes = new byte[valueLength];
                    dis.readFully(valueBytes);
                    String value = new String(valueBytes, "UTF-8");
                    
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    double height = dis.readDouble();
                    
                    AttributeEntity attr = new AttributeEntity(tag, value, x, y, height);
                    attr.setPrompt(prompt);
                    entities.add(attr);
                    
                } else if (entityType == TYPE_XREF) {
                    // XREF: pathLength, path, x, y
                    int pathLength = dis.readInt();
                    byte[] pathBytes = new byte[pathLength];
                    dis.readFully(pathBytes);
                    String path = new String(pathBytes, "UTF-8");
                    double x = dis.readDouble();
                    double y = dis.readDouble();
                    
                    entities.add(new XRefEntity(path, x, y));
                    
                } else if (entityType == TYPE_WIPEOUT) {
                    // WIPEOUT: numVertices (int), then vertex pairs
                    int numVertices = dis.readInt();
                    WipeoutEntity wipeout = new WipeoutEntity();
                    for (int i = 0; i < numVertices; i++) {
                        double x = dis.readDouble();
                        double y = dis.readDouble();
                        wipeout.addVertex(x, y);
                    }
                    entities.add(wipeout);
                    
                } else if (entityType == TYPE_3DFACE) {
                    // 3DFACE: x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4 (12 doubles)
                    double x1 = dis.readDouble();
                    double y1 = dis.readDouble();
                    double z1 = dis.readDouble();
                    double x2 = dis.readDouble();
                    double y2 = dis.readDouble();
                    double z2 = dis.readDouble();
                    double x3 = dis.readDouble();
                    double y3 = dis.readDouble();
                    double z3 = dis.readDouble();
                    double x4 = dis.readDouble();
                    double y4 = dis.readDouble();
                    double z4 = dis.readDouble();
                    entities.add(new Face3DEntity(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4));
                    
                } else if (entityType == TYPE_POLYFACE_MESH) {
                    // POLYFACE MESH: numVertices (int), vertices (x,y,z triplets)
                    int numVertices = dis.readInt();
                    PolyfaceMeshEntity mesh = new PolyfaceMeshEntity();
                    for (int i = 0; i < numVertices; i++) {
                        double x = dis.readDouble();
                        double y = dis.readDouble();
                        double z = dis.readDouble();
                        mesh.addVertex(x, y, z);
                    }
                    entities.add(mesh);
                    
                } else if (entityType == TYPE_MESH) {
                    // MESH: numVertices (int), subdivisionLevel (int), vertices
                    int numVertices = dis.readInt();
                    int subdivisionLevel = dis.readInt();
                    MeshEntity mesh = new MeshEntity();
                    mesh.setSubdivisionLevel(subdivisionLevel);
                    for (int i = 0; i < numVertices; i++) {
                        double x = dis.readDouble();
                        double y = dis.readDouble();
                        double z = dis.readDouble();
                        mesh.addVertex(x, y, z);
                    }
                    entities.add(mesh);
                    
                } else if (entityType == TYPE_3DSOLID) {
                    // 3DSOLID: bounding box (6 doubles), dataLength (int), data
                    double minX = dis.readDouble();
                    double minY = dis.readDouble();
                    double minZ = dis.readDouble();
                    double maxX = dis.readDouble();
                    double maxY = dis.readDouble();
                    double maxZ = dis.readDouble();
                    int dataLength = dis.readInt();
                    byte[] data = new byte[dataLength];
                    dis.readFully(data);
                    
                    Solid3DEntity solid = new Solid3DEntity();
                    solid.setBoundingBoxMinX(minX);
                    solid.setBoundingBoxMinY(minY);
                    solid.setBoundingBoxMinZ(minZ);
                    solid.setBoundingBoxMaxX(maxX);
                    solid.setBoundingBoxMaxY(maxY);
                    solid.setBoundingBoxMaxZ(maxZ);
                    solid.setProprietaryData(new String(data, "UTF-8"));
                    entities.add(solid);
                    
                } else if (entityType == TYPE_SURFACE) {
                    // SURFACE: uDegree, vDegree, numU, numV (4 ints), dataLength, data
                    int uDegree = dis.readInt();
                    int vDegree = dis.readInt();
                    int numU = dis.readInt();
                    int numV = dis.readInt();
                    int dataLength = dis.readInt();
                    byte[] data = new byte[dataLength];
                    dis.readFully(data);
                    
                    SurfaceEntity surface = new SurfaceEntity();
                    surface.setUDegree(uDegree);
                    surface.setVDegree(vDegree);
                    surface.setNumUControlPoints(numU);
                    surface.setNumVControlPoints(numV);
                    surface.setSurfaceData(new String(data, "UTF-8"));
                    entities.add(surface);
                    
                } else if (entityType == TYPE_BODY) {
                    // BODY: version (int), dataLength (int), ACIS data
                    int version = dis.readInt();
                    int dataLength = dis.readInt();
                    byte[] data = new byte[dataLength];
                    dis.readFully(data);
                    
                    BodyEntity body = new BodyEntity();
                    body.setVersion(version);
                    body.setAcisData(new String(data, "UTF-8"));
                    entities.add(body);
                    
                } else if (entityType == TYPE_REGION) {
                    // REGION: numVertices (int), vertices (x,y pairs), filled (boolean)
                    int numVertices = dis.readInt();
                    RegionEntity region = new RegionEntity();
                    for (int i = 0; i < numVertices; i++) {
                        double x = dis.readDouble();
                        double y = dis.readDouble();
                        region.addVertex(x, y);
                    }
                    region.setFilled(dis.readBoolean());
                    entities.add(region);
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
            
        } else if (entity instanceof TextEntity) {
            TextEntity text = (TextEntity) entity;
            writer.write("0\nTEXT\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", text.getX(), text.getY()));
            writer.write(String.format("40\n%.6f\n", text.getHeight()));
            writer.write(String.format("1\n%s\n", text.getText()));
            if (text.getRotationAngle() != 0) {
                writer.write(String.format("50\n%.6f\n", text.getRotationAngle()));
            }
            
        } else if (entity instanceof MTextEntity) {
            MTextEntity mtext = (MTextEntity) entity;
            writer.write("0\nMTEXT\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", mtext.getX(), mtext.getY()));
            writer.write(String.format("40\n%.6f\n", mtext.getHeight()));
            writer.write(String.format("41\n%.6f\n", mtext.getWidth()));
            writer.write(String.format("1\n%s\n", mtext.getText()));
            
        } else if (entity instanceof DimensionEntity) {
            DimensionEntity dim = (DimensionEntity) entity;
            writer.write("0\nDIMENSION\n8\n0\n");
            writer.write(String.format("70\n%d\n", dim.getDimensionType()));
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", dim.getX1(), dim.getY1()));
            writer.write(String.format("11\n%.6f\n21\n%.6f\n", dim.getX2(), dim.getY2()));
            writer.write(String.format("13\n%.6f\n23\n%.6f\n", dim.getTextX(), dim.getTextY()));
            writer.write(String.format("42\n%.6f\n", dim.getMeasurement()));
            
        } else if (entity instanceof LeaderEntity) {
            LeaderEntity leader = (LeaderEntity) entity;
            writer.write("0\nLEADER\n8\n0\n");
            writer.write(String.format("3\n%s\n", leader.getText()));
            List<Double> vertices = leader.getVertices();
            for (int i = 0; i < vertices.size(); i += 2) {
                writer.write(String.format("10\n%.6f\n20\n%.6f\n", vertices.get(i), vertices.get(i + 1)));
            }
            
        } else if (entity instanceof ToleranceEntity) {
            ToleranceEntity tolerance = (ToleranceEntity) entity;
            writer.write("0\nTOLERANCE\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", tolerance.getX(), tolerance.getY()));
            writer.write(String.format("40\n%.6f\n", tolerance.getHeight()));
            writer.write(String.format("1\n%s\n", tolerance.getToleranceString()));
            
        } else if (entity instanceof TableEntity) {
            TableEntity table = (TableEntity) entity;
            writer.write("0\nACDBTABLE\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", table.getX(), table.getY()));
            writer.write(String.format("90\n%d\n91\n%d\n", table.getRows(), table.getColumns()));
            writer.write(String.format("40\n%.6f\n41\n%.6f\n", table.getCellHeight(), table.getCellWidth()));
            for (String cellValue : table.getCellValues()) {
                writer.write(String.format("1\n%s\n", cellValue));
            }
            
        } else if (entity instanceof BlockEntity) {
            BlockEntity block = (BlockEntity) entity;
            writer.write("0\nBLOCK\n8\n0\n");
            writer.write(String.format("2\n%s\n", block.getName()));
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", block.getBaseX(), block.getBaseY()));
            
            // Write block entities
            for (DxfEntity blockEntity : block.getEntities()) {
                writeEntity(writer, blockEntity);
            }
            
            // End block
            writer.write("0\nENDBLK\n8\n0\n");
            
        } else if (entity instanceof InsertEntity) {
            InsertEntity insert = (InsertEntity) entity;
            writer.write("0\nINSERT\n8\n0\n");
            writer.write(String.format("2\n%s\n", insert.getBlockName()));
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", insert.getInsertX(), insert.getInsertY()));
            writer.write(String.format("41\n%.6f\n42\n%.6f\n", insert.getScaleX(), insert.getScaleY()));
            writer.write(String.format("50\n%.6f\n", insert.getRotation()));
            
        } else if (entity instanceof AttributeEntity) {
            AttributeEntity attr = (AttributeEntity) entity;
            writer.write("0\nATTRIB\n8\n0\n");
            writer.write(String.format("2\n%s\n", attr.getTag()));
            writer.write(String.format("3\n%s\n", attr.getPrompt()));
            writer.write(String.format("1\n%s\n", attr.getValue()));
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", attr.getX(), attr.getY()));
            writer.write(String.format("40\n%.6f\n", attr.getHeight()));
            
        } else if (entity instanceof XRefEntity) {
            XRefEntity xref = (XRefEntity) entity;
            writer.write("0\nXREF\n8\n0\n");
            writer.write(String.format("1\n%s\n", xref.getFilePath()));
            writer.write(String.format("10\n%.6f\n20\n%.6f\n", xref.getInsertX(), xref.getInsertY()));
            
        } else if (entity instanceof WipeoutEntity) {
            WipeoutEntity wipeout = (WipeoutEntity) entity;
            writer.write("0\nWIPEOUT\n8\n0\n");
            writer.write(String.format("90\n%d\n", wipeout.getVertexCount()));
            List<Double> vertices = wipeout.getVertices();
            for (int i = 0; i < vertices.size(); i += 2) {
                writer.write(String.format("10\n%.6f\n20\n%.6f\n", vertices.get(i), vertices.get(i + 1)));
            }
            
        } else if (entity instanceof Face3DEntity) {
            Face3DEntity face = (Face3DEntity) entity;
            writer.write("0\n3DFACE\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n30\n%.6f\n", face.getX1(), face.getY1(), face.getZ1()));
            writer.write(String.format("11\n%.6f\n21\n%.6f\n31\n%.6f\n", face.getX2(), face.getY2(), face.getZ2()));
            writer.write(String.format("12\n%.6f\n22\n%.6f\n32\n%.6f\n", face.getX3(), face.getY3(), face.getZ3()));
            writer.write(String.format("13\n%.6f\n23\n%.6f\n33\n%.6f\n", face.getX4(), face.getY4(), face.getZ4()));
            
        } else if (entity instanceof PolyfaceMeshEntity) {
            PolyfaceMeshEntity mesh = (PolyfaceMeshEntity) entity;
            writer.write("0\nPOLYLINE\n8\n0\n");
            writer.write("70\n64\n"); // Polyface mesh flag
            writer.write(String.format("71\n%d\n", mesh.getVertexCount()));
            List<Double> vertices = mesh.getVertices();
            for (int i = 0; i < vertices.size(); i += 3) {
                writer.write("0\nVERTEX\n8\n0\n");
                writer.write(String.format("10\n%.6f\n20\n%.6f\n30\n%.6f\n", 
                    vertices.get(i), vertices.get(i + 1), vertices.get(i + 2)));
            }
            writer.write("0\nSEQEND\n8\n0\n");
            
        } else if (entity instanceof MeshEntity) {
            MeshEntity mesh = (MeshEntity) entity;
            writer.write("0\nMESH\n8\n0\n");
            writer.write(String.format("91\n%d\n", mesh.getVertexCount()));
            writer.write(String.format("92\n%d\n", mesh.getSubdivisionLevel()));
            List<Double> vertices = mesh.getVertices();
            for (int i = 0; i < vertices.size(); i += 3) {
                writer.write(String.format("10\n%.6f\n20\n%.6f\n30\n%.6f\n", 
                    vertices.get(i), vertices.get(i + 1), vertices.get(i + 2)));
            }
            
        } else if (entity instanceof Solid3DEntity) {
            Solid3DEntity solid = (Solid3DEntity) entity;
            writer.write("0\n3DSOLID\n8\n0\n");
            writer.write(String.format("10\n%.6f\n20\n%.6f\n30\n%.6f\n", 
                solid.getBoundingBoxMinX(), solid.getBoundingBoxMinY(), solid.getBoundingBoxMinZ()));
            writer.write(String.format("11\n%.6f\n21\n%.6f\n31\n%.6f\n", 
                solid.getBoundingBoxMaxX(), solid.getBoundingBoxMaxY(), solid.getBoundingBoxMaxZ()));
            writer.write(String.format("1\n%s\n", solid.getProprietaryData()));
            
        } else if (entity instanceof SurfaceEntity) {
            SurfaceEntity surface = (SurfaceEntity) entity;
            writer.write("0\nSURFACE\n8\n0\n");
            writer.write(String.format("71\n%d\n72\n%d\n", surface.getUDegree(), surface.getVDegree()));
            writer.write(String.format("73\n%d\n74\n%d\n", 
                surface.getNumUControlPoints(), surface.getNumVControlPoints()));
            writer.write(String.format("1\n%s\n", surface.getSurfaceData()));
            
        } else if (entity instanceof BodyEntity) {
            BodyEntity body = (BodyEntity) entity;
            writer.write("0\nBODY\n8\n0\n");
            writer.write(String.format("70\n%d\n", body.getVersion()));
            writer.write(String.format("1\n%s\n", body.getAcisData()));
            
        } else if (entity instanceof RegionEntity) {
            RegionEntity region = (RegionEntity) entity;
            writer.write("0\nREGION\n8\n0\n");
            writer.write(String.format("90\n%d\n", region.getVertexCount()));
            List<Double> vertices = region.getVertices();
            for (int i = 0; i < vertices.size(); i += 2) {
                writer.write(String.format("10\n%.6f\n20\n%.6f\n", vertices.get(i), vertices.get(i + 1)));
            }
        }
    }
}
