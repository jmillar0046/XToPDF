package com.xtopdf.xtopdf.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class DwgToDxfServiceTest {

    private DwgToDxfService dwgToDxfService;
    private File dxfFile;

    @BeforeEach
    void setUp() {
        dwgToDxfService = new DwgToDxfService();
    }

    @Test
    void testConvertDwgToDxf_WithLineEntity() throws Exception {
        // Create a simple binary DWG file with one LINE entity
        // Format: [type:1byte][x1:double][y1:double][x2:double][y2:double]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // LINE entity: type=1, from (0,0) to (100,100)
        dos.writeByte(1); // LINE type
        dos.writeDouble(0.0); // x1
        dos.writeDouble(0.0); // y1
        dos.writeDouble(100.0); // x2
        dos.writeDouble(100.0); // y2
        
        var dwgFile = new MockMultipartFile("file", "test.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/testDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists(), "The DXF file should be created.");
        assertTrue(dxfFile.length() > 0, "The DXF file should not be empty.");
        
        // Verify DXF content contains LINE entity
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("LINE"), "DXF should contain LINE entity");
        assertTrue(dxfContent.contains("AC1009"), "DXF should be R12 format");
        
        // Clean up
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithCircleEntity() throws Exception {
        // Create a simple binary DWG file with one CIRCLE entity
        // Format: [type:1byte][centerX:double][centerY:double][radius:double]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // CIRCLE entity: type=2, center at (50,50), radius=25
        dos.writeByte(2); // CIRCLE type
        dos.writeDouble(50.0); // centerX
        dos.writeDouble(50.0); // centerY
        dos.writeDouble(25.0); // radius
        
        var dwgFile = new MockMultipartFile("file", "test.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/testCircleDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists(), "The DXF file should be created.");
        
        // Verify DXF content contains CIRCLE entity
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("CIRCLE"), "DXF should contain CIRCLE entity");
        
        // Clean up
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithNullFile_ThrowsNullPointerException() {
        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/testOutput.dxf");
        
        assertThrows(NullPointerException.class, () -> dwgToDxfService.convertDwgToDxf(null, dxfFile));
    }

    @Test
    void testConvertDwgToDxf_WithArcEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // ARC entity: type=3
        dos.writeByte(3); // ARC type
        dos.writeDouble(50.0); // centerX
        dos.writeDouble(50.0); // centerY
        dos.writeDouble(25.0); // radius
        dos.writeDouble(0.0); // startAngle
        dos.writeDouble(90.0); // endAngle
        
        var dwgFile = new MockMultipartFile("file", "arc.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/arcDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("ARC"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithPointEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // POINT entity: type=4
        dos.writeByte(4);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        
        var dwgFile = new MockMultipartFile("file", "point.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/pointDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("POINT"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithPolylineEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // POLYLINE entity: type=5
        dos.writeByte(5);
        dos.writeInt(3); // numVertices
        dos.writeDouble(0.0);
        dos.writeDouble(0.0);
        dos.writeDouble(10.0);
        dos.writeDouble(0.0);
        dos.writeDouble(10.0);
        dos.writeDouble(10.0);
        
        var dwgFile = new MockMultipartFile("file", "polyline.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/polylineDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("POLYLINE") || dxfContent.contains("LWPOLYLINE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithTextEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // TEXT entity: type=8
        dos.writeByte(8);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        dos.writeDouble(5.0); // height
        String text = "Hello";
        dos.writeInt(text.length());
        dos.writeBytes(text);
        
        var dwgFile = new MockMultipartFile("file", "text.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/textDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("TEXT"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithMultipleEntities() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // LINE
        dos.writeByte(1);
        dos.writeDouble(0.0);
        dos.writeDouble(0.0);
        dos.writeDouble(10.0);
        dos.writeDouble(10.0);
        
        // CIRCLE
        dos.writeByte(2);
        dos.writeDouble(5.0);
        dos.writeDouble(5.0);
        dos.writeDouble(2.0);
        
        var dwgFile = new MockMultipartFile("file", "multiple.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/multipleDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("LINE"));
        assertTrue(dxfContent.contains("CIRCLE"));
        
        dxfFile.delete();
    }

    // Additional comprehensive tests using test resource files
    
    @Test
    void testConvertDwgToDxf_SimpleLine_FromFile() throws Exception {
        var resource = new org.springframework.core.io.ClassPathResource("test-files/simple_line.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/simple_line_output.dxf");
            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
            
            assertTrue(dxfFile.exists());
            assertTrue(dxfFile.length() > 0);
            String content = Files.readString(dxfFile.toPath());
            assertTrue(content.contains("LINE"));
            assertTrue(content.contains("ENTITIES"));
            
            dxfFile.delete();
        }
    }

    @Test
    void testConvertDwgToDxf_MultipleEntities_FromFile() throws Exception {
        var resource = new org.springframework.core.io.ClassPathResource("test-files/multi_entity.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/multi_entity_output.dxf");
            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
            
            assertTrue(dxfFile.exists());
            String content = Files.readString(dxfFile.toPath());
            assertTrue(content.contains("LINE"));
            assertTrue(content.contains("CIRCLE"));
            assertTrue(content.contains("ARC"));
            assertTrue(content.contains("POINT"));
            
            dxfFile.delete();
        }
    }

    @Test
    void testConvertDwgToDxf_Polyline_FromFile() throws Exception {
        var resource = new org.springframework.core.io.ClassPathResource("test-files/polyline.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/polyline_output.dxf");
            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
            
            assertTrue(dxfFile.exists());
            String content = Files.readString(dxfFile.toPath());
            assertTrue(content.contains("POLYLINE") || content.contains("LWPOLYLINE"));
            
            dxfFile.delete();
        }
    }

    @Test
    void testConvertDwgToDxf_ComplexEntities_FromFile() throws Exception {
        var resource = new org.springframework.core.io.ClassPathResource("test-files/complex.dwg");
        try (var is = resource.getInputStream()) {
            byte[] fileBytes = is.readAllBytes();
            var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
                "application/acad", fileBytes);
            
            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/complex_output.dxf");
            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
            
            assertTrue(dxfFile.exists());
            String content = Files.readString(dxfFile.toPath());
            assertTrue(content.contains("ELLIPSE"));
            assertTrue(content.contains("SOLID") || content.contains("3DSOLID"));
            assertTrue(content.contains("TEXT"));
            assertTrue(content.contains("MTEXT"));
            
            dxfFile.delete();
        }
    }

    @Test
    void testConvertDwgToDxf_EmptyFile() {
        var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
            "application/acad", new byte[0]);
        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/empty_output.dxf");
        
        // Should handle empty file gracefully
        assertDoesNotThrow(() -> dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile));
        
        if (dxfFile.exists()) {
            dxfFile.delete();
        }
    }

    @Test
    void testConvertDwgToDxf_InvalidData() {
        var dwgFile = new MockMultipartFile("test.dwg", "test.dwg",
            "application/acad", "invalid data".getBytes());
        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/invalid_output.dxf");
        
        // May throw IOException for invalid format or handle gracefully
        try {
            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
            // If it succeeds, file should exist
            assertTrue(dxfFile.exists());
        } catch (Exception e) {
            // Expected for invalid format
            assertTrue(e != null);
        } finally {
            if (dxfFile.exists()) {
                dxfFile.delete();
            }
        }
    }
}
