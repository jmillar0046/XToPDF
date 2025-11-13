package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * REGION entity - 2D enclosed area (often used for Boolean operations).
 * DWG format: type=25, numVertices, vertices..., filled
 * DXF group codes: 90 (vertex count), multiple 10/20 for vertices
 * 
 * For PDF rendering, we render as filled polygon or outline.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RegionEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // x1, y1, x2, y2, ...
    private boolean filled = true;
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public int getVertexCount() { 
        return vertices.size() / 2; 
    }
}
