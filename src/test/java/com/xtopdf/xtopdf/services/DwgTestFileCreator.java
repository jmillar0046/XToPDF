package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates binary DWG test files for testing DwgToDxfService
 */
class DwgTestFileCreator {
    
    private static final String BASE_DIR = "src/test/resources/test-files/";
    
    @Test
    void createDwgTestFiles() throws Exception {
        new File(BASE_DIR).mkdirs();
        
        createSimpleDwgWithLine();
        createDwgWithMultipleEntities();
        createDwgWithPolyline();
        createComplexDwg();
    }
    
    private void createSimpleDwgWithLine() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(BASE_DIR + "simple_line.dwg"))) {
            // TYPE_LINE = 1
            dos.writeByte(1);
            // x1, y1, x2, y2
            dos.writeDouble(0.0);
            dos.writeDouble(0.0);
            dos.writeDouble(100.0);
            dos.writeDouble(100.0);
        }
    }
    
    private void createDwgWithMultipleEntities() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(BASE_DIR + "multi_entity.dwg"))) {
            
            // LINE (type=1)
            dos.writeByte(1);
            dos.writeDouble(0.0);
            dos.writeDouble(0.0);
            dos.writeDouble(50.0);
            dos.writeDouble(50.0);
            
            // CIRCLE (type=2)
            dos.writeByte(2);
            dos.writeDouble(100.0); // centerX
            dos.writeDouble(100.0); // centerY
            dos.writeDouble(25.0);  // radius
            
            // ARC (type=3)
            dos.writeByte(3);
            dos.writeDouble(150.0); // centerX
            dos.writeDouble(150.0); // centerY
            dos.writeDouble(30.0);  // radius
            dos.writeDouble(0.0);   // startAngle
            dos.writeDouble(90.0);  // endAngle
            
            // POINT (type=4)
            dos.writeByte(4);
            dos.writeDouble(200.0);
            dos.writeDouble(200.0);
        }
    }
    
    private void createDwgWithPolyline() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(BASE_DIR + "polyline.dwg"))) {
            
            // POLYLINE (type=5)
            dos.writeByte(5);
            dos.writeInt(4); // 4 vertices
            // Vertices
            dos.writeDouble(0.0);
            dos.writeDouble(0.0);
            dos.writeDouble(10.0);
            dos.writeDouble(0.0);
            dos.writeDouble(10.0);
            dos.writeDouble(10.0);
            dos.writeDouble(0.0);
            dos.writeDouble(10.0);
        }
    }
    
    private void createComplexDwg() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new FileOutputStream(BASE_DIR + "complex.dwg"))) {
            
            // ELLIPSE (type=6)
            dos.writeByte(6);
            dos.writeDouble(50.0);  // centerX
            dos.writeDouble(50.0);  // centerY
            dos.writeDouble(30.0);  // majorAxisX
            dos.writeDouble(0.0);   // majorAxisY
            dos.writeDouble(0.5);   // ratio
            dos.writeDouble(0.0);   // startParam
            dos.writeDouble(6.28);  // endParam
            
            // SOLID (type=7)
            dos.writeByte(7);
            dos.writeDouble(100.0); // x1
            dos.writeDouble(100.0); // y1
            dos.writeDouble(150.0); // x2
            dos.writeDouble(100.0); // y2
            dos.writeDouble(150.0); // x3
            dos.writeDouble(150.0); // y3
            dos.writeDouble(100.0); // x4
            dos.writeDouble(150.0); // y4
            
            // TEXT (type=8)
            dos.writeByte(8);
            dos.writeDouble(10.0);  // x
            dos.writeDouble(10.0);  // y
            dos.writeDouble(5.0);   // height
            String text = "Test";
            dos.writeInt(text.length());
            dos.writeBytes(text);
            
            // MTEXT (type=9)
            dos.writeByte(9);
            dos.writeDouble(20.0);  // x
            dos.writeDouble(20.0);  // y
            dos.writeDouble(40.0);  // width
            dos.writeDouble(3.0);   // height
            String mtext = "Multi-line Text";
            dos.writeInt(mtext.length());
            dos.writeBytes(mtext);
        }
    }
}
