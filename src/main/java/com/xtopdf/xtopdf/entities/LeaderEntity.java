package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * LEADER/MULTILEADER entity - Arrow with annotation text.
 * DWG format: type=11, numVertices, vertices..., textX, textY, text (variable)
 * DXF group codes: 3 (text), multiple 10/20 pairs for vertices
 */
public class LeaderEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // Leader line vertices (x1, y1, x2, y2, ...)
    private String text = "";
    private double textX;
    private double textY;
    
    public LeaderEntity() {}
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public List<Double> getVertices() { return vertices; }
    public void setVertices(List<Double> vertices) { this.vertices = vertices; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public double getTextX() { return textX; }
    public void setTextX(double textX) { this.textX = textX; }
    
    public double getTextY() { return textY; }
    public void setTextY(double textY) { this.textY = textY; }
    
    public int getVertexCount() { return vertices.size() / 2; }
}
