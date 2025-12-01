package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for DxfEntityRenderer.
 */
class DxfEntityRendererTest {

    private DxfEntityRenderer renderer;
    private DxfPdfRenderer mockPdfRenderer;
    private PdfDocumentBuilder mockBuilder;
    private Map<String, BlockEntity> blockRegistry;

    @BeforeEach
    void setUp() {
        blockRegistry = new HashMap<>();
        renderer = new DxfEntityRenderer(blockRegistry);
        
        mockBuilder = mock(PdfDocumentBuilder.class);
        mockPdfRenderer = new DxfPdfRenderer(mockBuilder);
    }

    @Test
    void testRenderEntity_LineEntity() throws IOException {
        LineEntity line = new LineEntity();
        line.setX1(10);
        line.setY1(20);
        line.setX2(100);
        line.setY2(200);

        // Should not throw exception
        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, line, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_CircleEntity() throws IOException {
        CircleEntity circle = new CircleEntity();
        circle.setCenterX(50);
        circle.setCenterY(50);
        circle.setRadius(25);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, circle, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_ArcEntity() throws IOException {
        ArcEntity arc = new ArcEntity();
        arc.setCenterX(50);
        arc.setCenterY(50);
        arc.setRadius(30);
        arc.setStartAngle(0);
        arc.setEndAngle(90);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, arc, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_PointEntity() throws IOException {
        PointEntity point = new PointEntity();
        point.setX(10);
        point.setY(20);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, point, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_PolylineEntity() throws IOException {
        PolylineEntity polyline = new PolylineEntity();
        polyline.addVertex(0, 0);
        polyline.addVertex(50, 50);
        polyline.addVertex(100, 0);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, polyline, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_EllipseEntity() throws IOException {
        EllipseEntity ellipse = new EllipseEntity();
        ellipse.setCenterX(50);
        ellipse.setCenterY(50);
        ellipse.setMajorAxisX(30);
        ellipse.setMajorAxisY(0);
        ellipse.setRatio(0.5);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, ellipse, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_SolidEntity() throws IOException {
        SolidEntity solid = new SolidEntity();
        solid.setX1(0);
        solid.setY1(0);
        solid.setX2(50);
        solid.setY2(0);
        solid.setX3(50);
        solid.setY3(50);
        solid.setX4(0);
        solid.setY4(50);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, solid, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_TextEntity() throws IOException {
        TextEntity text = new TextEntity();
        text.setX(10);
        text.setY(20);
        text.setText("Test Text");
        text.setHeight(12);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, text, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_WithScale() throws IOException {
        LineEntity line = new LineEntity();
        line.setX1(10);
        line.setY1(20);
        line.setX2(100);
        line.setY2(200);

        // Render with different scale
        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, line, 2.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_WithOffset() throws IOException {
        CircleEntity circle = new CircleEntity();
        circle.setCenterX(50);
        circle.setCenterY(50);
        circle.setRadius(25);

        // Render with offset
        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, circle, 1.0, 100, 100, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_NullEntity() {
        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, null, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_InsertEntity_WithBlock() throws IOException {
        // Create a block with a line
        BlockEntity block = new BlockEntity();
        block.setName("TestBlock");
        block.setBaseX(0);
        block.setBaseY(0);
        
        LineEntity lineInBlock = new LineEntity();
        lineInBlock.setX1(0);
        lineInBlock.setY1(0);
        lineInBlock.setX2(50);
        lineInBlock.setY2(50);
        block.addEntity(lineInBlock);
        
        blockRegistry.put("TestBlock", block);

        // Create insert entity
        InsertEntity insert = new InsertEntity();
        insert.setBlockName("TestBlock");
        insert.setInsertX(100);
        insert.setInsertY(100);
        insert.setScaleX(1.0);
        insert.setScaleY(1.0);
        insert.setRotation(0);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, insert, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_InsertEntity_BlockNotFound() throws IOException {
        // Create insert entity referencing non-existent block
        InsertEntity insert = new InsertEntity();
        insert.setBlockName("NonExistentBlock");
        insert.setInsertX(100);
        insert.setInsertY(100);
        insert.setScaleX(1.0);
        insert.setScaleY(1.0);
        insert.setRotation(0);

        // Should handle gracefully without throwing
        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, insert, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_PolylineEntity_Closed() throws IOException {
        PolylineEntity polyline = new PolylineEntity();
        polyline.addVertex(0, 0);
        polyline.addVertex(50, 50);
        polyline.addVertex(100, 0);
        polyline.setClosed(true);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, polyline, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_AttributeEntity_WithValue() throws IOException {
        AttributeEntity attr = new AttributeEntity();
        attr.setX(10);
        attr.setY(20);
        attr.setValue("Test Attribute");
        attr.setHeight(12);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, attr, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_AttributeEntity_EmptyValue() throws IOException {
        AttributeEntity attr = new AttributeEntity();
        attr.setX(10);
        attr.setY(20);
        attr.setValue("");
        attr.setHeight(12);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, attr, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_PolyfaceMeshEntity() throws IOException {
        PolyfaceMeshEntity mesh = new PolyfaceMeshEntity();
        mesh.addVertex(0, 0, 0);
        mesh.addVertex(50, 50, 10);
        mesh.addVertex(100, 0, 0);
        mesh.addVertex(150, 50, 10);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, mesh, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_RegionEntity_Filled() throws IOException {
        RegionEntity region = new RegionEntity();
        region.addVertex(0, 0);
        region.addVertex(50, 0);
        region.addVertex(50, 50);
        region.addVertex(0, 50);
        region.setFilled(true);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, region, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_RegionEntity_NotFilled() throws IOException {
        RegionEntity region = new RegionEntity();
        region.addVertex(0, 0);
        region.addVertex(50, 0);
        region.addVertex(50, 50);
        region.setFilled(false);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, region, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_ViewportEntity() throws IOException {
        ViewportEntity viewport = new ViewportEntity();
        viewport.setCenterX(100);
        viewport.setCenterY(100);
        viewport.setWidth(200);
        viewport.setHeight(150);
        viewport.setScale(0.5);

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, viewport, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_ImageEntity() throws IOException {
        ImageEntity image = new ImageEntity();
        image.setInsertX(10);
        image.setInsertY(20);
        image.setWidth(100);
        image.setHeight(80);
        image.setImagePath("/path/to/image.png");

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, image, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_UnderlayEntity() throws IOException {
        UnderlayEntity underlay = new UnderlayEntity();
        underlay.setInsertX(10);
        underlay.setInsertY(20);
        underlay.setScaleX(1.5);
        underlay.setScaleY(1.5);
        underlay.setUnderlayPath("/path/to/underlay.pdf");
        underlay.setUnderlayType("PDF");

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, underlay, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }

    @Test
    void testRenderEntity_OleFrameEntity() throws IOException {
        OleFrameEntity ole = new OleFrameEntity();
        ole.setInsertX(10);
        ole.setInsertY(20);
        ole.setWidth(100);
        ole.setHeight(80);
        ole.setOleVersion(2);
        ole.setOleType("ExcelWorksheet");

        assertDoesNotThrow(() -> 
            renderer.renderEntity(mockPdfRenderer, ole, 1.0, 0, 0, 1.0, 1.0, 0.0)
        );
    }
}
