package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * MESH entity - Advanced mesh geometry (ACIS-like).
 * DWG format: type=21, numVertices, vertices...
 * DXF group codes: 91 (vertex count), multiple 10/20/30 for vertices
 * 
 * For PDF rendering, we project to 2D and render as wireframe or point cloud.
 * Note: Full MESH rendering requires advanced subdivision surface algorithms.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MeshEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // x1, y1, z1, x2, y2, z2, ...
    private int subdivisionLevel = 0;
    
    public void addVertex(double x, double y, double z) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
    }
    
    public int getVertexCount() { 
        return vertices.size() / 3; 
    }
}
