package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * TEXT entity - Single-line text.
 * DWG format: type=8, x, y, height, text (2 doubles + 1 double + string)
 * DXF group codes: 10/20 (insertion point), 40 (height), 1 (text value), 50 (rotation angle)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextEntity extends DxfEntity {
    private double x;
    private double y;
    private double height = 10.0; // Default text height
    private String text = "";
    private double rotationAngle = 0.0; // in degrees
    
    public TextEntity(double x, double y, double height, String text) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.text = text;
    }
}
