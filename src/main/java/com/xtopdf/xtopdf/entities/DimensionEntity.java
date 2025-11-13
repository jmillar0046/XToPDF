package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DIMENSION entity - Linear, radial, angular dimensions.
 * DWG format: type=10, dimType, x1, y1, x2, y2, textX, textY, measurement (1 byte + 7 doubles)
 * DXF group codes: 70 (dimension type), 10/20, 11/21 (extension points), 13/23 (definition point), 42 (actual measurement)
 * 
 * Dimension types:
 * 0 = Linear/aligned
 * 1 = Radial
 * 2 = Angular
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DimensionEntity extends DxfEntity {
    private int dimensionType = 0; // 0=linear, 1=radial, 2=angular
    private double x1, y1; // First extension line origin
    private double x2, y2; // Second extension line origin
    private double textX, textY; // Dimension text position
    private double measurement; // Actual measurement value
    
    public DimensionEntity(int dimensionType, double x1, double y1, double x2, double y2, 
                          double textX, double textY, double measurement) {
        this.dimensionType = dimensionType;
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.textX = textX; this.textY = textY;
        this.measurement = measurement;
    }
}
