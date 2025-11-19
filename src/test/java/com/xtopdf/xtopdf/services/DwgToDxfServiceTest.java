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

    @Test
    void testConvertDwgToDxf_WithEllipseEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // ELLIPSE entity: type=6
        dos.writeByte(6);
        dos.writeDouble(50.0); // centerX
        dos.writeDouble(50.0); // centerY
        dos.writeDouble(30.0); // majorAxisX
        dos.writeDouble(0.0); // majorAxisY
        dos.writeDouble(0.5); // ratio
        dos.writeDouble(0.0); // startParam
        dos.writeDouble(6.28318); // endParam (2*PI)
        
        var dwgFile = new MockMultipartFile("file", "ellipse.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/ellipseDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("ELLIPSE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithSolidEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // SOLID entity: type=7
        dos.writeByte(7);
        dos.writeDouble(0.0); // x1
        dos.writeDouble(0.0); // y1
        dos.writeDouble(10.0); // x2
        dos.writeDouble(0.0); // y2
        dos.writeDouble(10.0); // x3
        dos.writeDouble(10.0); // y3
        dos.writeDouble(0.0); // x4
        dos.writeDouble(10.0); // y4
        
        var dwgFile = new MockMultipartFile("file", "solid.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/solidDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("SOLID"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithMTextEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // MTEXT entity: type=9
        dos.writeByte(9);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        dos.writeDouble(50.0); // width
        dos.writeDouble(5.0); // height
        String text = "Multi-line text";
        dos.writeInt(text.length());
        dos.writeBytes(text);
        
        var dwgFile = new MockMultipartFile("file", "mtext.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/mtextDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("MTEXT"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithDimensionEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // DIMENSION entity: type=10
        dos.writeByte(10);
        dos.writeByte(0); // dimType
        dos.writeDouble(0.0); // x1
        dos.writeDouble(0.0); // y1
        dos.writeDouble(100.0); // x2
        dos.writeDouble(0.0); // y2
        dos.writeDouble(50.0); // textX
        dos.writeDouble(10.0); // textY
        dos.writeDouble(100.0); // measurement
        
        var dwgFile = new MockMultipartFile("file", "dimension.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/dimensionDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("DIMENSION"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithLeaderEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // LEADER entity: type=11
        dos.writeByte(11);
        dos.writeInt(2); // numVertices
        dos.writeDouble(0.0); // x1
        dos.writeDouble(0.0); // y1
        dos.writeDouble(10.0); // x2
        dos.writeDouble(10.0); // y2
        dos.writeDouble(15.0); // textX
        dos.writeDouble(15.0); // textY
        String text = "Leader";
        dos.writeInt(text.length());
        dos.writeBytes(text);
        
        var dwgFile = new MockMultipartFile("file", "leader.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/leaderDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("LEADER"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithToleranceEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // TOLERANCE entity: type=12
        dos.writeByte(12);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        dos.writeDouble(5.0); // height
        String text = "±0.01";
        dos.writeInt(text.length());
        dos.writeBytes(text);
        
        var dwgFile = new MockMultipartFile("file", "tolerance.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/toleranceDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("TOLERANCE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithTableEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // TABLE entity: type=13
        dos.writeByte(13);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        dos.writeInt(2); // rows
        dos.writeInt(2); // columns
        dos.writeDouble(10.0); // cellHeight
        dos.writeDouble(20.0); // cellWidth
        // 4 cells
        dos.writeInt(2);
        dos.writeBytes("C1");
        dos.writeInt(2);
        dos.writeBytes("C2");
        dos.writeInt(2);
        dos.writeBytes("C3");
        dos.writeInt(2);
        dos.writeBytes("C4");
        
        var dwgFile = new MockMultipartFile("file", "table.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/tableDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("TABLE") || dxfContent.contains("ACAD_TABLE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithBlockEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // BLOCK entity: type=14
        dos.writeByte(14);
        String blockName = "TestBlock";
        dos.writeInt(blockName.length());
        dos.writeBytes(blockName);
        dos.writeDouble(0.0); // baseX
        dos.writeDouble(0.0); // baseY
        dos.writeInt(0); // numEntities
        
        var dwgFile = new MockMultipartFile("file", "block.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/blockDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("BLOCK"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithInsertEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // INSERT entity: type=15
        dos.writeByte(15);
        String blockName = "TestBlock";
        dos.writeInt(blockName.length());
        dos.writeBytes(blockName);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        dos.writeDouble(1.0); // scaleX
        dos.writeDouble(1.0); // scaleY
        dos.writeDouble(0.0); // rotation
        
        var dwgFile = new MockMultipartFile("file", "insert.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/insertDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("INSERT"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithAttribEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // ATTRIB entity: type=16
        dos.writeByte(16);
        String tag = "TAG1";
        dos.writeInt(tag.length());
        dos.writeBytes(tag);
        String prompt = "Enter value:";
        dos.writeInt(prompt.length());
        dos.writeBytes(prompt);
        String value = "Value1";
        dos.writeInt(value.length());
        dos.writeBytes(value);
        dos.writeDouble(10.0); // x
        dos.writeDouble(20.0); // y
        dos.writeDouble(5.0); // height
        
        var dwgFile = new MockMultipartFile("file", "attrib.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/attribDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("ATTRIB"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithXRefEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // XREF entity: type=17
        dos.writeByte(17);
        String path = "/path/to/xref.dwg";
        dos.writeInt(path.length());
        dos.writeBytes(path);
        dos.writeDouble(0.0); // x
        dos.writeDouble(0.0); // y
        
        var dwgFile = new MockMultipartFile("file", "xref.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/xrefDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("XREF") || dxfContent.contains("INSERT"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithWipeoutEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // WIPEOUT entity: type=18
        dos.writeByte(18);
        dos.writeInt(3); // numVertices
        dos.writeDouble(0.0);
        dos.writeDouble(0.0);
        dos.writeDouble(10.0);
        dos.writeDouble(0.0);
        dos.writeDouble(10.0);
        dos.writeDouble(10.0);
        
        var dwgFile = new MockMultipartFile("file", "wipeout.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/wipeoutDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("WIPEOUT"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_With3DFaceEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // 3DFACE entity: type=19
        dos.writeByte(19);
        dos.writeDouble(0.0); // x1
        dos.writeDouble(0.0); // y1
        dos.writeDouble(0.0); // z1
        dos.writeDouble(10.0); // x2
        dos.writeDouble(0.0); // y2
        dos.writeDouble(0.0); // z2
        dos.writeDouble(10.0); // x3
        dos.writeDouble(10.0); // y3
        dos.writeDouble(0.0); // z3
        dos.writeDouble(0.0); // x4
        dos.writeDouble(10.0); // y4
        dos.writeDouble(0.0); // z4
        
        var dwgFile = new MockMultipartFile("file", "3dface.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/3dfaceDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("3DFACE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithPolyfaceMeshEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // POLYFACE_MESH entity: type=20
        dos.writeByte(20);
        dos.writeInt(3); // numVertices
        dos.writeDouble(0.0);
        dos.writeDouble(0.0);
        dos.writeDouble(0.0);
        dos.writeDouble(10.0);
        dos.writeDouble(0.0);
        dos.writeDouble(0.0);
        dos.writeDouble(5.0);
        dos.writeDouble(10.0);
        dos.writeDouble(5.0);
        
        var dwgFile = new MockMultipartFile("file", "polyfacemesh.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/polyfacemeshDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("POLYLINE") || dxfContent.contains("MESH"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithMeshEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // MESH entity: type=21
        dos.writeByte(21);
        dos.writeInt(4); // numVertices
        dos.writeInt(2); // subdivisionLevel
        // 4 vertices
        for (int i = 0; i < 4; i++) {
            dos.writeDouble(i * 10.0); // x
            dos.writeDouble(i * 10.0); // y
            dos.writeDouble(0.0); // z
        }
        
        var dwgFile = new MockMultipartFile("file", "mesh.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/meshDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("MESH") || dxfContent.contains("POLYLINE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_With3DSolidEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // 3DSOLID entity: type=22
        dos.writeByte(22);
        // Bounding box (6 doubles)
        dos.writeDouble(0.0); // minX
        dos.writeDouble(0.0); // minY
        dos.writeDouble(0.0); // minZ
        dos.writeDouble(10.0); // maxX
        dos.writeDouble(10.0); // maxY
        dos.writeDouble(10.0); // maxZ
        dos.writeInt(4); // dataLength
        dos.writeBytes("DATA");
        
        var dwgFile = new MockMultipartFile("file", "3dsolid.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/3dsolidDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("3DSOLID"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithSurfaceEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // SURFACE entity: type=23
        dos.writeByte(23);
        dos.writeInt(3); // uDegree
        dos.writeInt(3); // vDegree
        dos.writeInt(4); // numU
        dos.writeInt(4); // numV
        dos.writeInt(4); // dataLength
        dos.writeBytes("DATA");
        
        var dwgFile = new MockMultipartFile("file", "surface.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/surfaceDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("SURFACE"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithBodyEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // BODY entity: type=24
        dos.writeByte(24);
        dos.writeInt(1); // version
        dos.writeInt(4); // dataLength
        dos.writeBytes("DATA");
        
        var dwgFile = new MockMultipartFile("file", "body.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/bodyDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("BODY"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_WithRegionEntity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // REGION entity: type=25
        dos.writeByte(25);
        dos.writeInt(3); // numVertices
        dos.writeDouble(0.0); // x1
        dos.writeDouble(0.0); // y1
        dos.writeDouble(10.0); // x2
        dos.writeDouble(0.0); // y2
        dos.writeDouble(10.0); // x3
        dos.writeDouble(10.0); // y3
        dos.writeBoolean(true); // filled
        
        var dwgFile = new MockMultipartFile("file", "region.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/regionDwgToDxfOutput.dxf");

        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        String dxfContent = Files.readString(dxfFile.toPath());
        assertTrue(dxfContent.contains("REGION"));
        
        dxfFile.delete();
    }

    @Test
    void testConvertDwgToDxf_UnsupportedEntityType() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        // Unknown entity type: 99 (not defined)
        dos.writeByte(99);
        
        var dwgFile = new MockMultipartFile("file", "unknown.dwg", 
            MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

        dxfFile = new File(System.getProperty("java.io.tmpdir") + "/unknownDwgToDxfOutput.dxf");

        // Should handle unknown types gracefully by skipping or completing
        dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);

        assertTrue(dxfFile.exists());
        
        dxfFile.delete();
    }

    // Test for error conditions
    
    @Test
    void testConvertDwgToDxf_ExcessivePolylineVertices() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        assertThrows(IOException.class, () -> {
            dos.writeByte(5); // POLYLINE
            dos.writeInt(200000); // Exceeds MAX_VERTICES
            
            var dwgFile = new MockMultipartFile("file", "test.dwg", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/error_output.dxf");

            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
        });
    }

    @Test
    void testConvertDwgToDxf_ExcessiveTextLength() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        assertThrows(IOException.class, () -> {
            dos.writeByte(8); // TEXT
            dos.writeDouble(10.0);
            dos.writeDouble(20.0);
            dos.writeDouble(5.0);
            dos.writeInt(200000); // Exceeds MAX_TEXT_LENGTH
            
            var dwgFile = new MockMultipartFile("file", "test.dwg", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/error_output.dxf");

            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
        });
    }

    @Test
    void testConvertDwgToDxf_ExcessiveTableCells() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        assertThrows(IOException.class, () -> {
            dos.writeByte(13); // TABLE
            dos.writeDouble(10.0);
            dos.writeDouble(20.0);
            dos.writeInt(20000); // rows
            dos.writeInt(20000); // columns - exceeds MAX_TABLE_CELLS
            
            var dwgFile = new MockMultipartFile("file", "test.dwg", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/error_output.dxf");

            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
        });
    }

    @Test
    void testConvertDwgToDxf_NegativeVertexCount() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        assertThrows(IOException.class, () -> {
            dos.writeByte(5); // POLYLINE
            dos.writeInt(-1); // Negative vertex count
            
            var dwgFile = new MockMultipartFile("file", "test.dwg", 
                MediaType.APPLICATION_OCTET_STREAM_VALUE, baos.toByteArray());

            dxfFile = new File(System.getProperty("java.io.tmpdir") + "/error_output.dxf");

            dwgToDxfService.convertDwgToDxf(dwgFile, dxfFile);
        });
    }
}
