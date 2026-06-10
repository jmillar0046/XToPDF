package com.xtopdf.xtopdf.services.conversion.threed;

import net.jqwik.api.*;

import static net.jqwik.api.Arbitraries.floats;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for WireframeRenderer vertex projection bounds.
 *
 * Property 8: Wireframe vertex projection stays within render bounds
 * - For any vertex [x, y, z] contained within a valid BoundingBox3D
 *   (where minX ≤ x ≤ maxX, minY ≤ y ≤ maxY), the projected 2D coordinates
 *   from WireframeRenderer.projectVertex() satisfy:
 *   - OFFSET_X ≤ projectedX ≤ OFFSET_X + RENDER_WIDTH
 *   - OFFSET_Y - RENDER_HEIGHT ≤ projectedY ≤ OFFSET_Y
 *
 * **Validates: Requirements 10.1, 10.2**
 */
class WireframeProjectionPropertyTest {

    /**
     * Property 8: Projected X coordinate stays within [OFFSET_X, OFFSET_X + RENDER_WIDTH].
     *
     * **Validates: Requirements 10.1, 10.2**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 8: Wireframe vertex projection stays within render bounds")
    void projectedXStaysWithinRenderBounds(
            @ForAll("validBoundingBox") BoundingBox3D bbox,
            @ForAll("vertexWithinBox") float[] vertex) {

        // Constrain vertex to be within the provided bounding box
        float constrainedX = constrainToRange(vertex[0], bbox.minX(), bbox.maxX());
        float constrainedY = constrainToRange(vertex[1], bbox.minY(), bbox.maxY());
        float constrainedZ = constrainToRange(vertex[2], bbox.minZ(), bbox.maxZ());
        float[] constrainedVertex = new float[]{constrainedX, constrainedY, constrainedZ};

        float scale = WireframeRenderer.calculateScale(bbox);
        float[] projected = WireframeRenderer.projectVertex(constrainedVertex, bbox, scale);

        assertThat(projected[0])
                .as("Projected X should be >= OFFSET_X (%f) for vertex [%f, %f, %f] in bbox [%f..%f, %f..%f]",
                        WireframeRenderer.OFFSET_X, constrainedX, constrainedY, constrainedZ,
                        bbox.minX(), bbox.maxX(), bbox.minY(), bbox.maxY())
                .isGreaterThanOrEqualTo(WireframeRenderer.OFFSET_X);

        assertThat(projected[0])
                .as("Projected X should be <= OFFSET_X + RENDER_WIDTH (%f) for vertex [%f, %f, %f]",
                        WireframeRenderer.OFFSET_X + WireframeRenderer.RENDER_WIDTH,
                        constrainedX, constrainedY, constrainedZ)
                .isLessThanOrEqualTo(WireframeRenderer.OFFSET_X + WireframeRenderer.RENDER_WIDTH);
    }

    /**
     * Property 8: Projected Y coordinate stays within [OFFSET_Y - RENDER_HEIGHT, OFFSET_Y].
     *
     * **Validates: Requirements 10.1, 10.2**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 8: Wireframe vertex projection stays within render bounds")
    void projectedYStaysWithinRenderBounds(
            @ForAll("validBoundingBox") BoundingBox3D bbox,
            @ForAll("vertexWithinBox") float[] vertex) {

        // Constrain vertex to be within the provided bounding box
        float constrainedX = constrainToRange(vertex[0], bbox.minX(), bbox.maxX());
        float constrainedY = constrainToRange(vertex[1], bbox.minY(), bbox.maxY());
        float constrainedZ = constrainToRange(vertex[2], bbox.minZ(), bbox.maxZ());
        float[] constrainedVertex = new float[]{constrainedX, constrainedY, constrainedZ};

        float scale = WireframeRenderer.calculateScale(bbox);
        float[] projected = WireframeRenderer.projectVertex(constrainedVertex, bbox, scale);

        assertThat(projected[1])
                .as("Projected Y should be >= OFFSET_Y - RENDER_HEIGHT (%f) for vertex [%f, %f, %f]",
                        WireframeRenderer.OFFSET_Y - WireframeRenderer.RENDER_HEIGHT,
                        constrainedX, constrainedY, constrainedZ)
                .isGreaterThanOrEqualTo(WireframeRenderer.OFFSET_Y - WireframeRenderer.RENDER_HEIGHT);

        assertThat(projected[1])
                .as("Projected Y should be <= OFFSET_Y (%f) for vertex [%f, %f, %f]",
                        WireframeRenderer.OFFSET_Y, constrainedX, constrainedY, constrainedZ)
                .isLessThanOrEqualTo(WireframeRenderer.OFFSET_Y);
    }

    /**
     * Provides valid BoundingBox3D instances where min < max on all axes
     * and the box has non-zero dimensions.
     */
    @Provide
    Arbitrary<BoundingBox3D> validBoundingBox() {
        Arbitrary<Float> coords = floats().between(-1000f, 1000f);
        return Combinators.combine(coords, coords, coords, coords, coords, coords)
                .as((a, b, c, d, e, f) -> {
                    // Ensure min < max on all axes (add small offset to guarantee non-zero size)
                    float minX = Math.min(a, b);
                    float maxX = Math.max(a, b) + 0.01f;
                    float minY = Math.min(c, d);
                    float maxY = Math.max(c, d) + 0.01f;
                    float minZ = Math.min(e, f);
                    float maxZ = Math.max(e, f) + 0.01f;
                    return new BoundingBox3D(minX, maxX, minY, maxY, minZ, maxZ);
                });
    }

    /**
     * Provides vertex coordinates that will be constrained to the bounding box.
     * Values are generated in a wide range; the test constrains them to the actual bbox.
     */
    @Provide
    Arbitrary<float[]> vertexWithinBox() {
        return floats().between(-1000f, 1000f)
                .array(float[].class).ofSize(3);
    }

    /**
     * Constrains a value to be within [min, max].
     */
    private float constrainToRange(float value, float min, float max) {
        if (min >= max) {
            return min;
        }
        // Normalize value to [0, 1] range then scale to [min, max]
        float normalized = (value - (-1000f)) / (1000f - (-1000f));
        normalized = Math.max(0f, Math.min(1f, normalized));
        return min + normalized * (max - min);
    }
}
