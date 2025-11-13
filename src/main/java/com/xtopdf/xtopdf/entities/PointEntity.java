package com.xtopdf.xtopdf.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * POINT entity - Single coordinate marker.
 * DWG format: type=4, x, y (2 doubles)
 * DXF group codes: 10/20 (XY position)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PointEntity extends DxfEntity {
    private double x;
    private double y;
}
