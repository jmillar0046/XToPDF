package com.xtopdf.xtopdf.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for DXF entity classes to improve coverage.
 */
class SimpleEntityTests {

    @Test
    void testLineEntity() {
        LineEntity entity = new LineEntity(10.0, 20.0, 30.0, 40.0);
        assertEquals(10.0, entity.getX1());
        assertEquals(20.0, entity.getY1());
        assertEquals(30.0, entity.getX2());
        assertEquals(40.0, entity.getY2());
        assertNotNull(entity.toString());
    }

    @Test
    void testCircleEntity() {
        CircleEntity entity = new CircleEntity(50.0, 60.0, 10.0);
        assertEquals(50.0, entity.getCenterX());
        assertEquals(60.0, entity.getCenterY());
        assertEquals(10.0, entity.getRadius());
    }

    @Test
    void testArcEntity() {
        ArcEntity entity = new ArcEntity();
        entity.setCenterX(10.0);
        entity.setCenterY(20.0);
        entity.setRadius(15.0);
        entity.setStartAngle(0.0);
        entity.setEndAngle(90.0);
        assertEquals(15.0, entity.getRadius());
    }

    @Test
    void testEllipseEntity() {
        EllipseEntity entity = new EllipseEntity(10.0, 20.0, 30.0, 40.0, 0.5);
        assertEquals(0.5, entity.getRatio());
    }

    @Test
    void testPointEntity() {
        PointEntity entity = new PointEntity();
        entity.setX(100.0);
        entity.setY(200.0);
        assertEquals(100.0, entity.getX());
        assertEquals(200.0, entity.getY());
    }

    @Test
    void testPolylineEntity() {
        PolylineEntity entity = new PolylineEntity();
        entity.addVertex(10.0, 20.0);
        entity.addVertex(30.0, 40.0);
        assertEquals(2, entity.getVertexCount());
    }

    @Test
    void testTextEntity() {
        TextEntity entity = new TextEntity();
        entity.setText("Test");
        entity.setX(10.0);
        entity.setY(20.0);
        entity.setHeight(12.0);
        assertEquals("Test", entity.getText());
    }

    @Test
    void testMTextEntity() {
        MTextEntity entity = new MTextEntity();
        entity.setText("Multiline");
        entity.setX(5.0);
        entity.setY(10.0);
        assertEquals("Multiline", entity.getText());
    }

    @Test
    void testBlockEntity() {
        BlockEntity entity = new BlockEntity("TestBlock");
        assertEquals("TestBlock", entity.getName());
        entity.addEntity(new LineEntity());
        assertTrue(entity.getEntities().size() > 0);
    }

    @Test
    void testInsertEntity() {
        InsertEntity entity = new InsertEntity("Block1", 15.0, 25.0);
        assertEquals("Block1", entity.getBlockName());
    }

    @Test
    void testSolidEntity() {
        SolidEntity entity = new SolidEntity();
        entity.setX1(1.0);
        assertEquals(1.0, entity.getX1());
    }

    @Test
    void testAttributeEntity() {
        AttributeEntity entity = new AttributeEntity("TAG", "Value", 1.0, 2.0, 10.0);
        assertEquals("TAG", entity.getTag());
    }
}
