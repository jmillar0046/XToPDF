package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * WIPEOUT entity - Masking shape that covers underlying entities.
 * A filled polygon that acts as a mask to hide other content.
 * 
 * DWG format: type=18, numVertices, vertices... (variable)
 * DXF group codes: 90 (vertex count), multiple 10/20 pairs for vertices
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WipeoutEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // Stored as x1, y1, x2, y2, ...
    
    public void addVertex(double x, double y) {
        vertices.add(x);
        vertices.add(y);
    }
    
    public int getVertexCount() { 
        return vertices.size() / 2; 
    }
}
