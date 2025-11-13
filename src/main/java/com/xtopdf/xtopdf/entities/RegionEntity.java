package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * REGION entity - 2D enclosed area (often used for Boolean operations).
 * DWG format: type=25, numVertices, vertices..., filled
 * DXF group codes: 90 (vertex count), multiple 10/20 for vertices
 * 
 * For PDF rendering, we render as filled polygon or outline.
 */
public class RegionEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // x1, y1, x2, y2, ...
    private boolean filled = true;
    
    public RegionEntity() {}
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public List<Double> getVertices() { return vertices; }
    public void setVertices(List<Double> vertices) { this.vertices = vertices; }
    
    public int getVertexCount() { return vertices.size() / 2; }
    
    public boolean isFilled() { return filled; }
    public void setFilled(boolean filled) { this.filled = filled; }
}
