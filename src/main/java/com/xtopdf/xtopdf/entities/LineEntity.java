package com.xtopdf.xtopdf.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * LINE entity - Straight line between 2 points.
 * DWG format: type=1, x1, y1, x2, y2 (4 doubles)
 * DXF group codes: 10/20 (start XY), 11/21 (end XY)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LineEntity extends DxfEntity {
    private double x1;
    private double y1;
    private double x2;
    private double y2;
}
