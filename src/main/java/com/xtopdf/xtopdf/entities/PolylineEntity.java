package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * POLYLINE/LWPOLYLINE entity - Sequence of connected line segments.
 * DWG format: type=5, numVertices, x1, y1, x2, y2, ... (variable length)
 * DXF group codes: 90 (vertex count), multiple 10/20 pairs for vertices
 */
public class PolylineEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // Stored as x1, y1, x2, y2, ...
    private boolean closed = false;
    
    public PolylineEntity() {}
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public List<Double> getVertices() {
        return vertices;
    }
    
    public void setVertices(List<Double> vertices) {
        this.vertices = vertices;
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public int getVertexCount() {
        return vertices.size() / 2;
    }
}
