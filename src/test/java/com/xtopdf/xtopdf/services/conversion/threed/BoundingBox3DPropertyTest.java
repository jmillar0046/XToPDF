package com.xtopdf.xtopdf.services.conversion.threed;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for BoundingBox3D invariants.
 *
 * // Feature: converter-improvements, Property 9: BoundingBox3D expansion preserves invariants
 *
 * Property 9: BoundingBox3D expansion preserves invariants
 * - For any BoundingBox3D and any point (x, y, z), after expand():
 *   (a) minX ≤ maxX, minY ≤ maxY, minZ ≤ maxZ
 *   (b) width() ≥ 0, height() ≥ 0, depth() ≥ 0
 *   (c) the expanded box contains both the original box extents and the new point
 *
 * **Validates: Requirements 10.3**
 */
class BoundingBox3DPropertyTest {

    /**
     * Property 9: After expanding a BoundingBox3D with any point, min ≤ max on all axes.
     *
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 9: BoundingBox3D expansion preserves invariants")
    void expandedBoxPreservesMinMaxInvariants(
            @ForAll float initX,
            @ForAll float initY,
            @ForAll float initZ,
            @ForAll float expandX,
            @ForAll float expandY,
            @ForAll float expandZ) {

        BoundingBox3D initial = BoundingBox3D.initial(initX, initY, initZ);
        BoundingBox3D expanded = initial.expand(expandX, expandY, expandZ);

        assertThat(expanded.minX())
                .as("minX should be ≤ maxX after expand")
                .isLessThanOrEqualTo(expanded.maxX());
        assertThat(expanded.minY())
                .as("minY should be ≤ maxY after expand")
                .isLessThanOrEqualTo(expanded.maxY());
        assertThat(expanded.minZ())
                .as("minZ should be ≤ maxZ after expand")
                .isLessThanOrEqualTo(expanded.maxZ());
    }

    /**
     * Property 9: After expanding a BoundingBox3D, width/height/depth are non-negative.
     *
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 9: BoundingBox3D expansion preserves invariants")
    void expandedBoxHasNonNegativeDimensions(
            @ForAll float initX,
            @ForAll float initY,
            @ForAll float initZ,
            @ForAll float expandX,
            @ForAll float expandY,
            @ForAll float expandZ) {

        BoundingBox3D initial = BoundingBox3D.initial(initX, initY, initZ);
        BoundingBox3D expanded = initial.expand(expandX, expandY, expandZ);

        assertThat(expanded.width())
                .as("width should be ≥ 0 after expand")
                .isGreaterThanOrEqualTo(0f);
        assertThat(expanded.height())
                .as("height should be ≥ 0 after expand")
                .isGreaterThanOrEqualTo(0f);
        assertThat(expanded.depth())
                .as("depth should be ≥ 0 after expand")
                .isGreaterThanOrEqualTo(0f);
    }

    /**
     * Property 9: The expanded box contains the original extents and the new point.
     *
     * **Validates: Requirements 10.3**
     */
    @Property(tries = 25)
    @Tag("Feature: converter-improvements, Property 9: BoundingBox3D expansion preserves invariants")
    void expandedBoxContainsOriginalExtentsAndNewPoint(
            @ForAll float initX,
            @ForAll float initY,
            @ForAll float initZ,
            @ForAll float expandX,
            @ForAll float expandY,
            @ForAll float expandZ) {

        BoundingBox3D initial = BoundingBox3D.initial(initX, initY, initZ);
        BoundingBox3D expanded = initial.expand(expandX, expandY, expandZ);

        // Expanded box contains original extents
        assertThat(expanded.minX())
                .as("expanded minX should be ≤ original minX")
                .isLessThanOrEqualTo(initial.minX());
        assertThat(expanded.maxX())
                .as("expanded maxX should be ≥ original maxX")
                .isGreaterThanOrEqualTo(initial.maxX());
        assertThat(expanded.minY())
                .as("expanded minY should be ≤ original minY")
                .isLessThanOrEqualTo(initial.minY());
        assertThat(expanded.maxY())
                .as("expanded maxY should be ≥ original maxY")
                .isGreaterThanOrEqualTo(initial.maxY());
        assertThat(expanded.minZ())
                .as("expanded minZ should be ≤ original minZ")
                .isLessThanOrEqualTo(initial.minZ());
        assertThat(expanded.maxZ())
                .as("expanded maxZ should be ≥ original maxZ")
                .isGreaterThanOrEqualTo(initial.maxZ());

        // Expanded box contains the new point
        assertThat(expanded.minX())
                .as("expanded minX should be ≤ new point x")
                .isLessThanOrEqualTo(expandX);
        assertThat(expanded.maxX())
                .as("expanded maxX should be ≥ new point x")
                .isGreaterThanOrEqualTo(expandX);
        assertThat(expanded.minY())
                .as("expanded minY should be ≤ new point y")
                .isLessThanOrEqualTo(expandY);
        assertThat(expanded.maxY())
                .as("expanded maxY should be ≥ new point y")
                .isGreaterThanOrEqualTo(expandY);
        assertThat(expanded.minZ())
                .as("expanded minZ should be ≤ new point z")
                .isLessThanOrEqualTo(expandZ);
        assertThat(expanded.maxZ())
                .as("expanded maxZ should be ≥ new point z")
                .isGreaterThanOrEqualTo(expandZ);
    }
}
