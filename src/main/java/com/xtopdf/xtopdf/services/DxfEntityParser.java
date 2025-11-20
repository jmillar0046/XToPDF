package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for DXF (Drawing Exchange Format) files that extracts entities and blocks.
 */
public class DxfEntityParser {
    
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
    
    private final Map<String, BlockEntity> blockRegistry = new HashMap<>();
    
    /**
     * Safely parse an integer from user-controlled string input.
     */
    private int safeParseInt(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Cannot parse null or empty string");
        }
        long longValue = Long.parseLong(value.trim());
        if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
            throw new NumberFormatException("Value out of int range: " + longValue);
        }
        return (int) longValue;
    }
    
    /**
     * Safely parse a double from user-controlled string input.
     */
    private double safeParseDouble(String value) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException("Cannot parse null or empty string");
        }
        double doubleValue = Double.parseDouble(value.trim());
        if (!Double.isFinite(doubleValue)) {
            throw new NumberFormatException("Value is not finite: " + value);
        }
        return doubleValue;
    }
    
    /**
     * Safely cast a double to an int.
     */
    private int safeDoubleToInt(double value) throws NumberFormatException {
        if (!Double.isFinite(value)) {
            throw new NumberFormatException("Cannot cast non-finite value to int: " + value);
        }
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new NumberFormatException("Value out of int range: " + value);
        }
        return (int) value;
    }
    
    /**
     * Parse DXF entities from a multipart file.
     * 
     * @param dxfFile The DXF file to parse
     * @return List of parsed entities
     * @throws IOException if reading fails
     */
    public List<DxfEntity> parseDxfEntities(MultipartFile dxfFile) throws IOException {
        blockRegistry.clear(); // Reset block registry
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
    
    /**
     * Get the block registry with all parsed blocks.
     */
    public Map<String, BlockEntity> getBlockRegistry() {
        return blockRegistry;
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
}
