package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WireframeRenderer} utility class.
 *
 * Tests calculateScale(), projectVertex(), and renderEdges() methods.
 *
 * _Requirements: 10.1, 10.2_
 */
@ExtendWith(MockitoExtension.class)
class WireframeRendererTest {

    @Mock
    private PdfDocumentBuilder builder;

    @Nested
    @DisplayName("calculateScale()")
    class CalculateScale {

        @Test
        void zeroSizeBoundingBoxUsesMinimumDimension() {
            BoundingBox3D bbox = BoundingBox3D.initial(5f, 5f, 5f);

            float scale = WireframeRenderer.calculateScale(bbox);

            // width and height are 0, clamped to 0.001f
            // scale = min(400/0.001, 400/0.001) * 0.9 = 400000 * 0.9 = 360000
            assertThat(scale).as("zero-size box should use clamped minimum dimension")
                    .isEqualTo(Math.min(WireframeRenderer.RENDER_WIDTH / 0.001f,
                            WireframeRenderer.RENDER_HEIGHT / 0.001f) * 0.9f);
        }

        @Test
        void normalBoundingBoxCalculatesCorrectScale() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 100f, 0f, 200f, 0f, 50f);

            float scale = WireframeRenderer.calculateScale(bbox);

            // width=100, height=200
            // scale = min(400/100, 400/200) * 0.9 = min(4, 2) * 0.9 = 2 * 0.9 = 1.8
            assertThat(scale).as("scale should be constrained by the larger model dimension")
                    .isEqualTo(1.8f);
        }

        @Test
        void widerThanTallBoxConstrainedByWidth() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 800f, 0f, 100f, 0f, 50f);

            float scale = WireframeRenderer.calculateScale(bbox);

            // width=800, height=100
            // scale = min(400/800, 400/100) * 0.9 = min(0.5, 4) * 0.9 = 0.5 * 0.9 = 0.45
            assertThat(scale).as("scale should be constrained by width when wider than tall")
                    .isEqualTo(0.45f);
        }

        @Test
        void squareBoundingBoxProducesSymmetricScale() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 400f, 0f, 400f, 0f, 400f);

            float scale = WireframeRenderer.calculateScale(bbox);

            // width=400, height=400
            // scale = min(400/400, 400/400) * 0.9 = 1 * 0.9 = 0.9
            assertThat(scale).as("square box should produce scale of 0.9")
                    .isEqualTo(0.9f);
        }

        @Test
        void scaleIsAlwaysPositive() {
            BoundingBox3D bbox = new BoundingBox3D(-50f, 50f, -100f, 100f, 0f, 10f);

            float scale = WireframeRenderer.calculateScale(bbox);

            assertThat(scale).as("scale should always be positive").isGreaterThan(0f);
        }
    }

    @Nested
    @DisplayName("projectVertex()")
    class ProjectVertex {

        @Test
        void projectsMinCornerToOffsetOrigin() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 100f, 0f, 200f, 0f, 50f);
            float scale = WireframeRenderer.calculateScale(bbox);
            float[] vertex = {0f, 0f, 0f};

            float[] projected = WireframeRenderer.projectVertex(vertex, bbox, scale);

            // x = OFFSET_X + (0 - 0) * scale = 100
            // y = OFFSET_Y - (0 - 0) * scale = 350
            assertThat(projected[0]).as("min-corner x should project to OFFSET_X")
                    .isEqualTo(WireframeRenderer.OFFSET_X);
            assertThat(projected[1]).as("min-corner y should project to OFFSET_Y")
                    .isEqualTo(WireframeRenderer.OFFSET_Y);
        }

        @Test
        void projectsMaxCornerAwayFromOffset() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 100f, 0f, 200f, 0f, 50f);
            float scale = WireframeRenderer.calculateScale(bbox); // 1.8
            float[] vertex = {100f, 200f, 50f};

            float[] projected = WireframeRenderer.projectVertex(vertex, bbox, scale);

            // x = 100 + (100 - 0) * 1.8 = 100 + 180 = 280
            // y = 350 - (200 - 0) * 1.8 = 350 - 360 = -10
            assertThat(projected[0]).as("max-corner x projection")
                    .isEqualTo(WireframeRenderer.OFFSET_X + 100f * scale);
            assertThat(projected[1]).as("max-corner y projection")
                    .isEqualTo(WireframeRenderer.OFFSET_Y - 200f * scale);
        }

        @Test
        void projectsMidpointToMiddleOfRenderArea() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 100f, 0f, 100f, 0f, 100f);
            float scale = WireframeRenderer.calculateScale(bbox); // min(4,4)*0.9 = 3.6
            float[] vertex = {50f, 50f, 50f};

            float[] projected = WireframeRenderer.projectVertex(vertex, bbox, scale);

            // x = 100 + (50 - 0) * 3.6 = 100 + 180 = 280
            // y = 350 - (50 - 0) * 3.6 = 350 - 180 = 170
            assertThat(projected[0]).as("midpoint x projection")
                    .isEqualTo(WireframeRenderer.OFFSET_X + 50f * scale);
            assertThat(projected[1]).as("midpoint y projection")
                    .isEqualTo(WireframeRenderer.OFFSET_Y - 50f * scale);
        }

        @Test
        void projectsWithNegativeBoundingBoxOffset() {
            BoundingBox3D bbox = new BoundingBox3D(-50f, 50f, -50f, 50f, -50f, 50f);
            float scale = WireframeRenderer.calculateScale(bbox); // min(400/100, 400/100)*0.9 = 3.6
            float[] vertex = {0f, 0f, 0f};

            float[] projected = WireframeRenderer.projectVertex(vertex, bbox, scale);

            // x = 100 + (0 - (-50)) * 3.6 = 100 + 180 = 280
            // y = 350 - (0 - (-50)) * 3.6 = 350 - 180 = 170
            assertThat(projected[0]).as("vertex at origin with negative bbox min")
                    .isEqualTo(WireframeRenderer.OFFSET_X + 50f * scale);
            assertThat(projected[1]).as("vertex at origin with negative bbox min")
                    .isEqualTo(WireframeRenderer.OFFSET_Y - 50f * scale);
        }

        @Test
        void returnsArrayOfLengthTwo() {
            BoundingBox3D bbox = new BoundingBox3D(0f, 10f, 0f, 10f, 0f, 10f);
            float scale = 1.0f;
            float[] vertex = {5f, 5f, 5f};

            float[] projected = WireframeRenderer.projectVertex(vertex, bbox, scale);

            assertThat(projected).as("projected vertex should be 2D").hasSize(2);
        }
    }

    @Nested
    @DisplayName("renderEdges()")
    class RenderEdges {

        @Test
        void emptyVertexListDrawsNoLines() throws IOException {
            List<float[]> vertices = new ArrayList<>();
            List<int[]> edges = List.of(new int[]{0, 1});
            BoundingBox3D bbox = BoundingBox3D.initial(0f, 0f, 0f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 100);

            verifyNoInteractions(builder);
        }

        @Test
        void emptyEdgeListDrawsNoLines() throws IOException {
            List<float[]> vertices = List.of(new float[]{0f, 0f, 0f}, new float[]{1f, 1f, 1f});
            List<int[]> edges = new ArrayList<>();
            BoundingBox3D bbox = new BoundingBox3D(0f, 1f, 0f, 1f, 0f, 1f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 100);

            verifyNoInteractions(builder);
        }

        @Test
        void outOfBoundsEdgeIndicesAreSkipped() throws IOException {
            List<float[]> vertices = List.of(new float[]{0f, 0f, 0f}, new float[]{1f, 1f, 1f});
            List<int[]> edges = List.of(
                    new int[]{0, 5},   // index 5 out of bounds
                    new int[]{-1, 0},  // negative index
                    new int[]{3, 4}    // both out of bounds
            );
            BoundingBox3D bbox = new BoundingBox3D(0f, 1f, 0f, 1f, 0f, 1f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 100);

            verifyNoInteractions(builder);
        }

        @Test
        void validEdgeCallsDrawLine() throws IOException {
            List<float[]> vertices = List.of(
                    new float[]{0f, 0f, 0f},
                    new float[]{10f, 10f, 10f}
            );
            List<int[]> edges = List.of(new int[]{0, 1});
            BoundingBox3D bbox = new BoundingBox3D(0f, 10f, 0f, 10f, 0f, 10f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 100);

            float scale = WireframeRenderer.calculateScale(bbox);
            float[] p1 = WireframeRenderer.projectVertex(vertices.get(0), bbox, scale);
            float[] p2 = WireframeRenderer.projectVertex(vertices.get(1), bbox, scale);

            verify(builder, times(1)).drawLine(p1[0], p1[1], p2[0], p2[1]);
        }

        @Test
        void multipleValidEdgesCallDrawLineForEach() throws IOException {
            List<float[]> vertices = List.of(
                    new float[]{0f, 0f, 0f},
                    new float[]{5f, 0f, 0f},
                    new float[]{5f, 5f, 0f}
            );
            List<int[]> edges = List.of(
                    new int[]{0, 1},
                    new int[]{1, 2},
                    new int[]{2, 0}
            );
            BoundingBox3D bbox = new BoundingBox3D(0f, 5f, 0f, 5f, 0f, 0.001f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 100);

            verify(builder, times(3)).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
        }

        @Test
        void maxEdgesLimitsNumberOfDrawnLines() throws IOException {
            List<float[]> vertices = List.of(
                    new float[]{0f, 0f, 0f},
                    new float[]{1f, 0f, 0f},
                    new float[]{2f, 0f, 0f},
                    new float[]{3f, 0f, 0f}
            );
            List<int[]> edges = List.of(
                    new int[]{0, 1},
                    new int[]{1, 2},
                    new int[]{2, 3},
                    new int[]{3, 0}
            );
            BoundingBox3D bbox = new BoundingBox3D(0f, 3f, 0f, 0.001f, 0f, 0.001f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 2);

            verify(builder, times(2)).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
        }

        @Test
        void mixedValidAndInvalidEdgesOnlyDrawsValid() throws IOException {
            List<float[]> vertices = List.of(
                    new float[]{0f, 0f, 0f},
                    new float[]{5f, 5f, 5f}
            );
            List<int[]> edges = List.of(
                    new int[]{0, 1},   // valid
                    new int[]{0, 99},  // invalid
                    new int[]{1, 0}    // valid
            );
            BoundingBox3D bbox = new BoundingBox3D(0f, 5f, 0f, 5f, 0f, 5f);

            WireframeRenderer.renderEdges(builder, vertices, edges, bbox, 100);

            verify(builder, times(2)).drawLine(anyFloat(), anyFloat(), anyFloat(), anyFloat());
        }
    }
}
