package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 3DFACE entity - Polygon with 3-4 vertices (triangular or quadrilateral face).
 * DWG format: type=19, x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4
 * DXF group codes: 10/20/30, 11/21/31, 12/22/32, 13/23/33 (4 corner points with Z coordinates)
 * 
 * For PDF rendering, we project to 2D by ignoring Z coordinates and render as filled polygon.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Face3DEntity extends DxfEntity {
    private double x1, y1, z1;
    private double x2, y2, z2;
    private double x3, y3, z3;
    private double x4, y4, z4;
    private boolean isTriangle = false;
    
    public Face3DEntity(double x1, double y1, double z1, double x2, double y2, double z2, 
                        double x3, double y3, double z3) {
        this(x1, y1, z1, x2, y2, z2, x3, y3, z3, x3, y3, z3);
        this.isTriangle = true;
    }
    
    public Face3DEntity(double x1, double y1, double z1, double x2, double y2, double z2,
                        double x3, double y3, double z3, double x4, double y4, double z4) {
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.x2 = x2; this.y2 = y2; this.z2 = z2;
        this.x3 = x3; this.y3 = y3; this.z3 = z3;
        this.x4 = x4; this.y4 = y4; this.z4 = z4;
    }
}
