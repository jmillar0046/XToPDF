package com.xtopdf.xtopdf.services.conversion.threed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BoundingBox3D} record.
 *
 * Tests edge cases: single point, negative coordinates, very large values, zero-size bounding box.
 * Tests initial() factory, expand() method, and width()/height()/depth() calculations.
 *
 * _Requirements: 10.3_
 */
class BoundingBox3DTest {

    @Nested
    @DisplayName("initial() factory method")
    class InitialFactory {

        @Test
        void createsZeroSizeBoundingBoxFromSinglePoint() {
            BoundingBox3D box = BoundingBox3D.initial(5f, 10f, 15f);

            assertThat(box.minX()).isEqualTo(5f);
            assertThat(box.maxX()).isEqualTo(5f);
            assertThat(box.minY()).isEqualTo(10f);
            assertThat(box.maxY()).isEqualTo(10f);
            assertThat(box.minZ()).isEqualTo(15f);
            assertThat(box.maxZ()).isEqualTo(15f);
        }

        @Test
        void createsBoxAtOrigin() {
            BoundingBox3D box = BoundingBox3D.initial(0f, 0f, 0f);

            assertThat(box.minX()).isEqualTo(0f);
            assertThat(box.maxX()).isEqualTo(0f);
            assertThat(box.minY()).isEqualTo(0f);
            assertThat(box.maxY()).isEqualTo(0f);
            assertThat(box.minZ()).isEqualTo(0f);
            assertThat(box.maxZ()).isEqualTo(0f);
        }

        @Test
        void createsBoxWithNegativeCoordinates() {
            BoundingBox3D box = BoundingBox3D.initial(-3f, -7f, -11f);

            assertThat(box.minX()).isEqualTo(-3f);
            assertThat(box.maxX()).isEqualTo(-3f);
            assertThat(box.minY()).isEqualTo(-7f);
            assertThat(box.maxY()).isEqualTo(-7f);
            assertThat(box.minZ()).isEqualTo(-11f);
            assertThat(box.maxZ()).isEqualTo(-11f);
        }

        @Test
        void createsBoxWithVeryLargeValues() {
            BoundingBox3D box = BoundingBox3D.initial(1e30f, -1e30f, Float.MAX_VALUE);

            assertThat(box.minX()).isEqualTo(1e30f);
            assertThat(box.maxX()).isEqualTo(1e30f);
            assertThat(box.minY()).isEqualTo(-1e30f);
            assertThat(box.maxY()).isEqualTo(-1e30f);
            assertThat(box.minZ()).isEqualTo(Float.MAX_VALUE);
            assertThat(box.maxZ()).isEqualTo(Float.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("width(), height(), depth() calculations")
    class DimensionCalculations {

        @Test
        void zeroSizeBoxHasZeroDimensions() {
            BoundingBox3D box = BoundingBox3D.initial(5f, 10f, 15f);

            assertThat(box.width()).as("single point should have zero width").isEqualTo(0f);
            assertThat(box.height()).as("single point should have zero height").isEqualTo(0f);
            assertThat(box.depth()).as("single point should have zero depth").isEqualTo(0f);
        }

        @Test
        void calculatesCorrectDimensionsForNormalBox() {
            BoundingBox3D box = new BoundingBox3D(1f, 4f, 2f, 7f, 3f, 9f);

            assertThat(box.width()).isEqualTo(3f);
            assertThat(box.height()).isEqualTo(5f);
            assertThat(box.depth()).isEqualTo(6f);
        }

        @Test
        void calculatesCorrectDimensionsWithNegativeCoordinates() {
            BoundingBox3D box = new BoundingBox3D(-5f, 5f, -10f, -2f, -3f, 3f);

            assertThat(box.width()).isEqualTo(10f);
            assertThat(box.height()).isEqualTo(8f);
            assertThat(box.depth()).isEqualTo(6f);
        }

        @Test
        void calculatesCorrectDimensionsWithVeryLargeRange() {
            BoundingBox3D box = new BoundingBox3D(-1e20f, 1e20f, 0f, 1e20f, -1e20f, 0f);

            assertThat(box.width()).isEqualTo(2e20f);
            assertThat(box.height()).isEqualTo(1e20f);
            assertThat(box.depth()).isEqualTo(1e20f);
        }
    }

    @Nested
    @DisplayName("expand() method")
    class ExpandMethod {

        @Test
        void expandWithSamePointDoesNotChangeBox() {
            BoundingBox3D box = BoundingBox3D.initial(5f, 10f, 15f);
            BoundingBox3D expanded = box.expand(5f, 10f, 15f);

            assertThat(expanded).isEqualTo(box);
        }

        @Test
        void expandGrowsBoxToIncludeNewPointBeyondMax() {
            BoundingBox3D box = BoundingBox3D.initial(0f, 0f, 0f);
            BoundingBox3D expanded = box.expand(10f, 20f, 30f);

            assertThat(expanded.minX()).isEqualTo(0f);
            assertThat(expanded.maxX()).isEqualTo(10f);
            assertThat(expanded.minY()).isEqualTo(0f);
            assertThat(expanded.maxY()).isEqualTo(20f);
            assertThat(expanded.minZ()).isEqualTo(0f);
            assertThat(expanded.maxZ()).isEqualTo(30f);
        }

        @Test
        void expandGrowsBoxToIncludeNewPointBeyondMin() {
            BoundingBox3D box = BoundingBox3D.initial(10f, 10f, 10f);
            BoundingBox3D expanded = box.expand(-5f, -3f, -1f);

            assertThat(expanded.minX()).isEqualTo(-5f);
            assertThat(expanded.maxX()).isEqualTo(10f);
            assertThat(expanded.minY()).isEqualTo(-3f);
            assertThat(expanded.maxY()).isEqualTo(10f);
            assertThat(expanded.minZ()).isEqualTo(-1f);
            assertThat(expanded.maxZ()).isEqualTo(10f);
        }

        @Test
        void expandWithPointInsideBoxDoesNotChangeBox() {
            BoundingBox3D box = new BoundingBox3D(-10f, 10f, -10f, 10f, -10f, 10f);
            BoundingBox3D expanded = box.expand(5f, -5f, 0f);

            assertThat(expanded).as("point inside box should not change bounds").isEqualTo(box);
        }

        @Test
        void multipleExpandsAccumulateCorrectly() {
            BoundingBox3D box = BoundingBox3D.initial(0f, 0f, 0f);
            box = box.expand(1f, 2f, 3f);
            box = box.expand(-1f, -2f, -3f);
            box = box.expand(0.5f, 0.5f, 0.5f);

            assertThat(box.minX()).isEqualTo(-1f);
            assertThat(box.maxX()).isEqualTo(1f);
            assertThat(box.minY()).isEqualTo(-2f);
            assertThat(box.maxY()).isEqualTo(2f);
            assertThat(box.minZ()).isEqualTo(-3f);
            assertThat(box.maxZ()).isEqualTo(3f);
        }

        @Test
        void expandWithNegativeCoordinatesOnAllAxes() {
            BoundingBox3D box = BoundingBox3D.initial(-100f, -100f, -100f);
            BoundingBox3D expanded = box.expand(-200f, -50f, -150f);

            assertThat(expanded.minX()).isEqualTo(-200f);
            assertThat(expanded.maxX()).isEqualTo(-100f);
            assertThat(expanded.minY()).isEqualTo(-100f);
            assertThat(expanded.maxY()).isEqualTo(-50f);
            assertThat(expanded.minZ()).isEqualTo(-150f);
            assertThat(expanded.maxZ()).isEqualTo(-100f);
        }

        @Test
        void expandWithVeryLargeValues() {
            BoundingBox3D box = BoundingBox3D.initial(0f, 0f, 0f);
            BoundingBox3D expanded = box.expand(1e30f, -1e30f, Float.MAX_VALUE);

            assertThat(expanded.minX()).isEqualTo(0f);
            assertThat(expanded.maxX()).isEqualTo(1e30f);
            assertThat(expanded.minY()).isEqualTo(-1e30f);
            assertThat(expanded.maxY()).isEqualTo(0f);
            assertThat(expanded.minZ()).isEqualTo(0f);
            assertThat(expanded.maxZ()).isEqualTo(Float.MAX_VALUE);
        }

        @Test
        void expandPreservesNonNegativeDimensions() {
            BoundingBox3D box = BoundingBox3D.initial(5f, 5f, 5f);
            BoundingBox3D expanded = box.expand(-5f, -5f, -5f);

            assertThat(expanded.width()).as("width should be non-negative").isGreaterThanOrEqualTo(0f);
            assertThat(expanded.height()).as("height should be non-negative").isGreaterThanOrEqualTo(0f);
            assertThat(expanded.depth()).as("depth should be non-negative").isGreaterThanOrEqualTo(0f);
        }

        @Test
        void expandIsImmutable() {
            BoundingBox3D original = BoundingBox3D.initial(1f, 2f, 3f);
            BoundingBox3D expanded = original.expand(10f, 20f, 30f);

            assertThat(original.maxX()).as("original should not be mutated").isEqualTo(1f);
            assertThat(expanded.maxX()).isEqualTo(10f);
        }
    }
}
