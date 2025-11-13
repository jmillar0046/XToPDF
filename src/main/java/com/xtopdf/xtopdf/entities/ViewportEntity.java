package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * VIEWPORT entity - Defines visible area in paper space/layouts.
 * DWG format: type=26, x, y, width, height, scale (5 doubles)
 * DXF group codes: 10/20 (center), 40/41 (width/height), 45 (view scale)
 * 
 * For PDF rendering, we display as a clipping rectangle outline.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ViewportEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double width = 100.0;
    private double height = 100.0;
    private double scale = 1.0;
    
    public ViewportEntity(double centerX, double centerY, double width, double height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }
}
