package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * WIPEOUT entity - Masking shape that covers underlying entities.
 * A filled polygon that acts as a mask to hide other content.
 * 
 * DWG format: type=18, numVertices, vertices... (variable)
 * DXF group codes: 90 (vertex count), multiple 10/20 pairs for vertices
 */
public class WipeoutEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // Stored as x1, y1, x2, y2, ...
    
    public WipeoutEntity() {}
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public List<Double> getVertices() { return vertices; }
    public void setVertices(List<Double> vertices) { this.vertices = vertices; }
    
    public int getVertexCount() { return vertices.size() / 2; }
}
