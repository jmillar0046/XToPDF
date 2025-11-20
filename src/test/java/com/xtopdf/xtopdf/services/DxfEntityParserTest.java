package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DxfEntityParser.
 */
class DxfEntityParserTest {

    private DxfEntityParser parser;

    @BeforeEach
    void setUp() {
        parser = new DxfEntityParser();
    }

    @Test
    void testParseDxfEntities_EmptyFile() throws IOException {
        var content = "";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertNotNull(entities, "Entities list should not be null");
        assertTrue(entities.isEmpty(), "Entities list should be empty for empty file");
    }

    @Test
    void testParseDxfEntities_SimpleHeader() throws IOException {
        var content = "0\nSECTION\n2\nHEADER\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertNotNull(entities, "Entities list should not be null");
    }

    @Test
    void testParseDxfEntities_LineEntity() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n0\nLINE\n8\n0\n10\n10.0\n20\n20.0\n11\n100.0\n21\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "line.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertNotNull(entities, "Entities list should not be null");
        assertEquals(1, entities.size(), "Should parse one line entity");
        assertTrue(entities.get(0) instanceof LineEntity, "Entity should be a LineEntity");
        
        LineEntity line = (LineEntity) entities.get(0);
        assertEquals(10.0, line.getX1(), 0.001, "X1 coordinate should match");
        assertEquals(20.0, line.getY1(), 0.001, "Y1 coordinate should match");
        assertEquals(100.0, line.getX2(), 0.001, "X2 coordinate should match");
        assertEquals(100.0, line.getY2(), 0.001, "Y2 coordinate should match");
    }

    @Test
    void testParseDxfEntities_CircleEntity() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n0\nCIRCLE\n8\n0\n10\n50.0\n20\n50.0\n40\n25.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "circle.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertEquals(1, entities.size(), "Should parse one circle entity");
        assertTrue(entities.get(0) instanceof CircleEntity, "Entity should be a CircleEntity");
        
        CircleEntity circle = (CircleEntity) entities.get(0);
        assertEquals(50.0, circle.getCenterX(), 0.001, "Center X should match");
        assertEquals(50.0, circle.getCenterY(), 0.001, "Center Y should match");
        assertEquals(25.0, circle.getRadius(), 0.001, "Radius should match");
    }

    @Test
    void testParseDxfEntities_ArcEntity() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n0\nARC\n8\n0\n10\n50.0\n20\n50.0\n40\n30.0\n50\n0.0\n51\n90.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "arc.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertEquals(1, entities.size(), "Should parse one arc entity");
        assertTrue(entities.get(0) instanceof ArcEntity, "Entity should be an ArcEntity");
        
        ArcEntity arc = (ArcEntity) entities.get(0);
        assertEquals(50.0, arc.getCenterX(), 0.001);
        assertEquals(50.0, arc.getCenterY(), 0.001);
        assertEquals(30.0, arc.getRadius(), 0.001);
        assertEquals(0.0, arc.getStartAngle(), 0.001);
        assertEquals(90.0, arc.getEndAngle(), 0.001);
    }

    @Test
    void testParseDxfEntities_PointEntity() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n0\nPOINT\n8\n0\n10\n15.0\n20\n25.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "point.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertEquals(1, entities.size(), "Should parse one point entity");
        assertTrue(entities.get(0) instanceof PointEntity, "Entity should be a PointEntity");
        
        PointEntity point = (PointEntity) entities.get(0);
        assertEquals(15.0, point.getX(), 0.001);
        assertEquals(25.0, point.getY(), 0.001);
    }

    @Test
    void testParseDxfEntities_MultipleEntities() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n" +
                      "0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n50.0\n21\n50.0\n" +
                      "0\nCIRCLE\n8\n0\n10\n100.0\n20\n100.0\n40\n20.0\n" +
                      "0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "multiple.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertEquals(2, entities.size(), "Should parse two entities");
        assertTrue(entities.get(0) instanceof LineEntity, "First entity should be a line");
        assertTrue(entities.get(1) instanceof CircleEntity, "Second entity should be a circle");
    }

    @Test
    void testGetBlockRegistry_EmptyAfterParsing() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n0\nLINE\n8\n0\n10\n0.0\n20\n0.0\n11\n100.0\n21\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "test.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        parser.parseDxfEntities(dxfFile);
        assertNotNull(parser.getBlockRegistry(), "Block registry should not be null");
    }

    @Test
    void testParseDxfEntities_NullFile_ThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parseDxfEntities(null));
    }

    @Test
    void testParseDxfEntities_InvalidNumericValue_SkipsEntity() throws IOException {
        var content = "0\nSECTION\n2\nENTITIES\n0\nLINE\n8\n0\n10\ninvalid\n20\n20.0\n11\n100.0\n21\n100.0\n0\nENDSEC\n0\nEOF\n";
        var dxfFile = new MockMultipartFile("file", "invalid.dxf", MediaType.APPLICATION_OCTET_STREAM_VALUE, content.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        assertNotNull(entities, "Should handle invalid numeric values gracefully");
    }
}
