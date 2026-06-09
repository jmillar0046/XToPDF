package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;
import com.xtopdf.xtopdf.services.dxf.DxfEntityParser;
import net.jqwik.api.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Property-based tests for CAD services (DxfEntityParser).
 *
 * Verifies that various DXF content is parsed without crashing
 * and produces the expected entity types.
 *
 * **Validates: Requirements 32.3**
 */
class DxfParsingPropertyTest {

    private final DxfEntityParser parser = new DxfEntityParser();

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: DXF LINE entities parse correctly for various coordinates")
    void dxfLineEntitiesParseForVariousCoordinates(
            @ForAll("coordinates") double x1,
            @ForAll("coordinates") double y1,
            @ForAll("coordinates") double x2,
            @ForAll("coordinates") double y2) throws IOException {

        String dxfContent = buildDxfWithLine(x1, y1, x2, y2);
        MockMultipartFile dxfFile = new MockMultipartFile(
                "file", "test.dxf", "application/octet-stream", dxfContent.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);

        assertThat(entities).hasSize(1);
        assertThat(entities.get(0)).isInstanceOf(LineEntity.class);

        LineEntity line = (LineEntity) entities.get(0);
        assertThat(line.getX1()).isEqualTo(x1, org.assertj.core.data.Offset.offset(0.001));
        assertThat(line.getY1()).isEqualTo(y1, org.assertj.core.data.Offset.offset(0.001));
        assertThat(line.getX2()).isEqualTo(x2, org.assertj.core.data.Offset.offset(0.001));
        assertThat(line.getY2()).isEqualTo(y2, org.assertj.core.data.Offset.offset(0.001));
    }

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: DXF CIRCLE entities parse correctly for various radii")
    void dxfCircleEntitiesParseForVariousRadii(
            @ForAll("coordinates") double cx,
            @ForAll("coordinates") double cy,
            @ForAll("positiveValues") double radius) throws IOException {

        String dxfContent = buildDxfWithCircle(cx, cy, radius);
        MockMultipartFile dxfFile = new MockMultipartFile(
                "file", "test.dxf", "application/octet-stream", dxfContent.getBytes());

        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);

        assertThat(entities).hasSize(1);
        assertThat(entities.get(0)).isInstanceOf(CircleEntity.class);

        CircleEntity circle = (CircleEntity) entities.get(0);
        assertThat(circle.getCenterX()).isEqualTo(cx, org.assertj.core.data.Offset.offset(0.001));
        assertThat(circle.getCenterY()).isEqualTo(cy, org.assertj.core.data.Offset.offset(0.001));
        assertThat(circle.getRadius()).isEqualTo(radius, org.assertj.core.data.Offset.offset(0.001));
    }

    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property: DXF parser does not crash on various entity counts")
    void dxfParserHandlesMultipleEntities(
            @ForAll("entityCounts") int count) throws IOException {

        String dxfContent = buildDxfWithMultipleLines(count);
        MockMultipartFile dxfFile = new MockMultipartFile(
                "file", "test.dxf", "application/octet-stream", dxfContent.getBytes());

        assertThatNoException().isThrownBy(() -> {
            List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
            assertThat(entities).hasSize(count);
        });
    }

    // ---------------------------------------------------------------
    // Generators
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<Double> coordinates() {
        return Arbitraries.doubles().between(-1000.0, 1000.0)
                .filter(Double::isFinite);
    }

    @Provide
    Arbitrary<Double> positiveValues() {
        return Arbitraries.doubles().between(0.1, 500.0)
                .filter(Double::isFinite);
    }

    @Provide
    Arbitrary<Integer> entityCounts() {
        return Arbitraries.integers().between(1, 20);
    }

    // ---------------------------------------------------------------
    // DXF Content Builders
    // ---------------------------------------------------------------

    private String buildDxfWithLine(double x1, double y1, double x2, double y2) {
        return "0\nSECTION\n2\nENTITIES\n" +
                "0\nLINE\n" +
                "10\n" + x1 + "\n" +
                "20\n" + y1 + "\n" +
                "11\n" + x2 + "\n" +
                "21\n" + y2 + "\n" +
                "0\nENDSEC\n0\nEOF\n";
    }

    private String buildDxfWithCircle(double cx, double cy, double radius) {
        return "0\nSECTION\n2\nENTITIES\n" +
                "0\nCIRCLE\n" +
                "10\n" + cx + "\n" +
                "20\n" + cy + "\n" +
                "40\n" + radius + "\n" +
                "0\nENDSEC\n0\nEOF\n";
    }

    private String buildDxfWithMultipleLines(int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("0\nSECTION\n2\nENTITIES\n");
        for (int i = 0; i < count; i++) {
            sb.append("0\nLINE\n");
            sb.append("10\n").append(i * 10.0).append("\n");
            sb.append("20\n").append(i * 10.0).append("\n");
            sb.append("11\n").append((i + 1) * 10.0).append("\n");
            sb.append("21\n").append((i + 1) * 10.0).append("\n");
        }
        sb.append("0\nENDSEC\n0\nEOF\n");
        return sb.toString();
    }
}
