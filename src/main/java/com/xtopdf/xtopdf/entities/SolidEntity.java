package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SOLID/TRACE entity - Filled triangle or quadrilateral.
 * DWG format: type=7, x1, y1, x2, y2, x3, y3, x4, y4 (8 doubles, x4/y4 optional if triangle)
 * DXF group codes: 10/20, 11/21, 12/22, 13/23 for four corners
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SolidEntity extends DxfEntity {
    private double x1, y1;
    private double x2, y2;
    private double x3, y3;
    private double x4, y4;
    private boolean isTriangle = false;
    
    public SolidEntity(double x1, double y1, double x2, double y2, double x3, double y3) {
        this(x1, y1, x2, y2, x3, y3, x3, y3);
        this.isTriangle = true;
    }
    
    public SolidEntity(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.x3 = x3; this.y3 = y3;
        this.x4 = x4; this.y4 = y4;
    }
}
