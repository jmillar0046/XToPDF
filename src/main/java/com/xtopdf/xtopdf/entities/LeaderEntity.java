package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * LEADER/MULTILEADER entity - Arrow with annotation text.
 * DWG format: type=11, numVertices, vertices..., textX, textY, text (variable)
 * DXF group codes: 3 (text), multiple 10/20 pairs for vertices
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeaderEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // Leader line vertices (x1, y1, x2, y2, ...)
    private String text = "";
    private double textX;
    private double textY;
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public int getVertexCount() { 
        return vertices.size() / 2; 
    }
}
