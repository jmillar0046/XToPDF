package com.xtopdf.xtopdf.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * CIRCLE entity - Circle defined by center + radius.
 * DWG format: type=2, centerX, centerY, radius (3 doubles)
 * DXF group codes: 10/20 (center XY), 40 (radius)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CircleEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double radius;
}
