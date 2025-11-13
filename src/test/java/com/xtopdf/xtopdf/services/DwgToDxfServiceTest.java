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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
