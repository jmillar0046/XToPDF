package com.xtopdf.xtopdf.services.conversion.threed;

import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;

import java.io.IOException;
import java.util.List;

/**
 * Stateless utility class for wireframe rendering of 3D models.
 * Projects 3D vertices onto a 2D plane and draws edges via PdfDocumentBuilder.
 */
public final class WireframeRenderer {

    private WireframeRenderer() {} // utility class

    public static final float RENDER_WIDTH = 400f;
    public static final float RENDER_HEIGHT = 400f;
    public static final float OFFSET_X = 100f;
    public static final float OFFSET_Y = 350f;

    /**
     * Calculates the scale factor to fit a bounding box within render dimensions.
     */
    public static float calculateScale(BoundingBox3D bbox) {
        float modelW = Math.max(bbox.width(), 0.001f);
        float modelH = Math.max(bbox.height(), 0.001f);
        return Math.min(RENDER_WIDTH / modelW, RENDER_HEIGHT / modelH) * 0.9f;
    }

    /**
     * Projects a 3D vertex onto 2D coordinates within the render area.
     */
    public static float[] projectVertex(float[] vertex, BoundingBox3D bbox, float scale) {
        float x = OFFSET_X + (vertex[0] - bbox.minX()) * scale;
        float y = OFFSET_Y - (vertex[1] - bbox.minY()) * scale;
        return new float[]{x, y};
    }

    /**
     * Renders edges by projecting 3D vertices and drawing lines via the PDF builder.
     * Skips edges with out-of-bounds vertex indices. Limits total edges drawn to maxEdges.
     */
    public static void renderEdges(PdfDocumentBuilder builder, List<float[]> vertices,
                                    List<int[]> edges, BoundingBox3D bbox, int maxEdges)
                                    throws IOException {
        if (bbox == null || vertices.isEmpty() || edges.isEmpty()) {
            return;
        }
        float scale = calculateScale(bbox);
        int count = Math.min(edges.size(), maxEdges);
        for (int i = 0; i < count; i++) {
            int[] edge = edges.get(i);
            if (edge[0] >= 0 && edge[0] < vertices.size()
                && edge[1] >= 0 && edge[1] < vertices.size()) {
                float[] p1 = projectVertex(vertices.get(edge[0]), bbox, scale);
                float[] p2 = projectVertex(vertices.get(edge[1]), bbox, scale);
                builder.drawLine(p1[0], p1[1], p2[0], p2[1]);
            }
        }
    }
}
