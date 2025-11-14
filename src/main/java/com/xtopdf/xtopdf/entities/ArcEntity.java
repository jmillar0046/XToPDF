package com.xtopdf.xtopdf.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ARC entity - Circular arc (portion of circle).
 * DWG format: type=3, centerX, centerY, radius, startAngle, endAngle (5 doubles)
 * DXF group codes: 10/20 (center XY), 40 (radius), 50 (start angle), 51 (end angle)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArcEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double radius;
    private double startAngle; // in degrees
    private double endAngle;   // in degrees
}
