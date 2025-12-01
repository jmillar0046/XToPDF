package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DxfCoordinateCalculator.
 */
class DxfCoordinateCalculatorTest {

    private DxfCoordinateCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DxfCoordinateCalculator();
    }

    @Test
    void testCalculateScale_EmptyEntities() {
        List<DxfEntity> entities = new ArrayList<>();
        double scale = calculator.calculateScale(entities, 595, 842);
        assertEquals(1.0, scale, "Scale should be 1.0 for empty entities");
    }

    @Test
    void testCalculateScale_SingleLineEntity() {
        List<DxfEntity> entities = new ArrayList<>();
        LineEntity line = new LineEntity();
        line.setX1(0);
        line.setY1(0);
        line.setX2(100);
        line.setY2(100);
        entities.add(line);

        double scale = calculator.calculateScale(entities, 595, 842);
        assertTrue(scale > 0, "Scale should be positive");
        assertTrue(scale < 10, "Scale should be reasonable");
    }

    @Test
    void testCalculateScale_MultipleEntities() {
        List<DxfEntity> entities = new ArrayList<>();
        
        LineEntity line = new LineEntity();
        line.setX1(0);
        line.setY1(0);
        line.setX2(50);
        line.setY2(50);
        entities.add(line);

        CircleEntity circle = new CircleEntity();
        circle.setCenterX(100);
        circle.setCenterY(100);
        circle.setRadius(20);
        entities.add(circle);

        double scale = calculator.calculateScale(entities, 595, 842);
        assertTrue(scale > 0, "Scale should be positive");
    }

    @Test
    void testCalculateScale_WithCircleEntity() {
        List<DxfEntity> entities = new ArrayList<>();
        CircleEntity circle = new CircleEntity();
        circle.setCenterX(50);
        circle.setCenterY(50);
        circle.setRadius(25);
        entities.add(circle);

        double scale = calculator.calculateScale(entities, 595, 842);
        assertTrue(scale > 0, "Scale should be positive for circle");
    }

    @Test
    void testCalculateScale_WithArcEntity() {
        List<DxfEntity> entities = new ArrayList<>();
        ArcEntity arc = new ArcEntity();
        arc.setCenterX(50);
        arc.setCenterY(50);
        arc.setRadius(30);
        arc.setStartAngle(0);
        arc.setEndAngle(90);
        entities.add(arc);

        double scale = calculator.calculateScale(entities, 595, 842);
        assertTrue(scale > 0, "Scale should be positive for arc");
    }

    @Test
    void testCalculateScale_WithPointEntity() {
        List<DxfEntity> entities = new ArrayList<>();
        PointEntity point = new PointEntity();
        point.setX(10);
        point.setY(20);
        entities.add(point);

        double scale = calculator.calculateScale(entities, 595, 842);
        assertTrue(scale > 0, "Scale should be positive for point");
    }

    @Test
    void testCalculateScale_FitsOnPage() {
        List<DxfEntity> entities = new ArrayList<>();
        LineEntity line = new LineEntity();
        line.setX1(0);
        line.setY1(0);
        line.setX2(200);
        line.setY2(200);
        entities.add(line);

        double pageWidth = 595;
        double pageHeight = 842;
        double scale = calculator.calculateScale(entities, pageWidth, pageHeight);
        
        // Verify the scaled content fits within page margins
        double scaledWidth = 200 * scale;
        double scaledHeight = 200 * scale;
        assertTrue(scaledWidth <= pageWidth - 100, "Scaled width should fit on page with margins");
        assertTrue(scaledHeight <= pageHeight - 100, "Scaled height should fit on page with margins");
    }
}
