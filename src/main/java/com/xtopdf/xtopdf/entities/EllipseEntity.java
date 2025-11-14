package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ELLIPSE entity - Ellipse (center, major/minor axis, start/end angle).
 * DWG format: type=6, centerX, centerY, majorAxisX, majorAxisY, ratio, startAngle, endAngle (7 doubles)
 * DXF group codes: 10/20 (center), 11/21 (major axis endpoint), 40 (ratio of minor to major axis), 41/42 (start/end parameters)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EllipseEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double majorAxisX; // Major axis endpoint relative to center
    private double majorAxisY;
    private double ratio;      // Ratio of minor axis to major axis
    private double startParam = 0;  // Start parameter (0 to 2*PI)
    private double endParam = Math.PI * 2; // End parameter
    
    public EllipseEntity(double centerX, double centerY, double majorAxisX, double majorAxisY, double ratio) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.majorAxisX = majorAxisX;
        this.majorAxisY = majorAxisY;
        this.ratio = ratio;
    }
}
