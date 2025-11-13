package com.xtopdf.xtopdf.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
 * - DWG is a binary file with simple records: type (1=LINE, 2=CIRCLE), followed by doubles for coordinates.
 * - Each entity record consists of:
 *   - 1 byte: entity type (1=LINE, 2=CIRCLE)
 *   - For LINE: 4 doubles (x1, y1, x2, y2)
 *   - For CIRCLE: 3 doubles (centerX, centerY, radius)
 * 
 * The output is a valid ASCII DXF file in R12 format (AC1009) containing HEADER, ENTITIES, and EOF sections.
 * DXF group codes used:
 * - 0: Entity type marker
 * - 8: Layer name (default "0")
 * - 10/20: Start point X/Y or center X/Y
 * - 11/21: End point X/Y (for LINE)
 * - 40: Radius (for CIRCLE)
 */
@Service
public class DwgToDxfService {
    
    // DWG entity type codes
    private static final byte TYPE_LINE = 1;
    private static final byte TYPE_CIRCLE = 2;
    
    public void convertDwgToDxf(MultipartFile dwgFile, File dxfFile) throws IOException {
        // Parse DWG binary format
        List<DwgEntity> entities = parseDwgFile(dwgFile);
        
        // Write DXF file in R12 format (AC1009)
        writeDxfFile(entities, dxfFile);
    }
    
    /**
     * Parse the simplified binary DWG format.
     * Format: [type:1byte][coordinates:doubles...]
     * - LINE (type=1): x1, y1, x2, y2 (4 doubles = 32 bytes)
     * - CIRCLE (type=2): centerX, centerY, radius (3 doubles = 24 bytes)
     */
    private List<DwgEntity> parseDwgFile(MultipartFile dwgFile) throws IOException {
        List<DwgEntity> entities = new ArrayList<>();
        
        try (InputStream is = dwgFile.getInputStream();
             DataInputStream dis = new DataInputStream(is)) {
            
            while (dis.available() > 0) {
                // Read entity type (1 byte)
                byte entityType = dis.readByte();
                
                if (entityType == TYPE_LINE) {
                    // Read LINE: x1, y1, x2, y2 (4 doubles)
                    // DXF group codes: 10=x1, 20=y1, 11=x2, 21=y2
                    double x1 = dis.readDouble();
                    double y1 = dis.readDouble();
                    double x2 = dis.readDouble();
                    double y2 = dis.readDouble();
                    entities.add(new LineEntity(x1, y1, x2, y2));
                } else if (entityType == TYPE_CIRCLE) {
                    // Read CIRCLE: centerX, centerY, radius (3 doubles)
                    // DXF group codes: 10=centerX, 20=centerY, 40=radius
                    double centerX = dis.readDouble();
                    double centerY = dis.readDouble();
                    double radius = dis.readDouble();
                    entities.add(new CircleEntity(centerX, centerY, radius));
                } else {
                    // Unknown entity type, skip to avoid corruption
                    // In a real implementation, you might want to log this
                }
            }
        }
        
        return entities;
    }
    
    /**
     * Write a valid ASCII DXF R12 file (AC1009).
     * Format includes: HEADER section, ENTITIES section, and EOF.
     */
    private void writeDxfFile(List<DwgEntity> entities, File dxfFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dxfFile))) {
            // Write HEADER section
            writer.write("0\n");
            writer.write("SECTION\n");
            writer.write("2\n");
            writer.write("HEADER\n");
            writer.write("9\n");
            writer.write("$ACADVER\n");
            writer.write("1\n");
            writer.write("AC1009\n"); // DXF R12 version
            writer.write("0\n");
            writer.write("ENDSEC\n");
            
            // Write ENTITIES section
            writer.write("0\n");
            writer.write("SECTION\n");
            writer.write("2\n");
            writer.write("ENTITIES\n");
            
            // Write each entity
            for (DwgEntity entity : entities) {
                if (entity instanceof LineEntity) {
                    LineEntity line = (LineEntity) entity;
                    writer.write("0\n");
                    writer.write("LINE\n");
                    writer.write("8\n");
                    writer.write("0\n"); // Layer 0 (default layer)
                    writer.write("10\n"); // Group code 10: Start X coordinate
                    writer.write(String.format("%.6f\n", line.x1));
                    writer.write("20\n"); // Group code 20: Start Y coordinate
                    writer.write(String.format("%.6f\n", line.y1));
                    writer.write("11\n"); // Group code 11: End X coordinate
                    writer.write(String.format("%.6f\n", line.x2));
                    writer.write("21\n"); // Group code 21: End Y coordinate
                    writer.write(String.format("%.6f\n", line.y2));
                } else if (entity instanceof CircleEntity) {
                    CircleEntity circle = (CircleEntity) entity;
                    writer.write("0\n");
                    writer.write("CIRCLE\n");
                    writer.write("8\n");
                    writer.write("0\n"); // Layer 0 (default layer)
                    writer.write("10\n"); // Group code 10: Center X coordinate
                    writer.write(String.format("%.6f\n", circle.centerX));
                    writer.write("20\n"); // Group code 20: Center Y coordinate
                    writer.write(String.format("%.6f\n", circle.centerY));
                    writer.write("40\n"); // Group code 40: Radius
                    writer.write(String.format("%.6f\n", circle.radius));
                }
            }
            
            // End ENTITIES section
            writer.write("0\n");
            writer.write("ENDSEC\n");
            
            // Write EOF
            writer.write("0\n");
            writer.write("EOF\n");
        }
    }
    
    // Inner classes for DWG entities parsed from binary format
    private abstract static class DwgEntity {}
    
    private static class LineEntity extends DwgEntity {
        double x1, y1, x2, y2;
        
        LineEntity(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }
    
    private static class CircleEntity extends DwgEntity {
        double centerX, centerY, radius;
        
        CircleEntity(double centerX, double centerY, double radius) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
        }
    }
}
