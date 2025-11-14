package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TOLERANCE entity - GD&T (Geometric Dimensioning and Tolerancing) annotation.
 * DWG format: type=12, x, y, height, text (3 doubles + string)
 * DXF group codes: 10/20 (insertion point), 40 (height), 1 (tolerance string)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToleranceEntity extends DxfEntity {
    private double x;
    private double y;
    private double height = 10.0;
    private String toleranceString = ""; // GD&T specification
    
    public ToleranceEntity(double x, double y, double height, String toleranceString) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.toleranceString = toleranceString;
    }
}
