package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * MTEXT entity - Multi-line text (formatted).
 * DWG format: type=9, x, y, width, height, text (4 doubles + string)
 * DXF group codes: 10/20 (insertion point), 40 (initial height), 41 (width), 1 (text), 7 (text style)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MTextEntity extends DxfEntity {
    private double x;
    private double y;
    private double width = 100.0;
    private double height = 10.0;
    private String text = "";
    
    public MTextEntity(double x, double y, double width, double height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }
}
