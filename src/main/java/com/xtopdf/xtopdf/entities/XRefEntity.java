package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * XREF entity - External reference (another DWG file).
 * References an external DWG file that should be loaded and rendered.
 * 
 * DWG format: type=17, pathLength, path, x, y (variable)
 * DXF group codes: 1 (file path), 10/20 (insertion point)
 * 
 * Note: For security and simplicity, this implementation documents the XREF
 * but does not automatically load external files. Users should manually include
 * referenced content or use specialized CAD tools.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class XRefEntity extends DxfEntity {
    private String filePath = "";
    private double insertX = 0.0;
    private double insertY = 0.0;
    
    public XRefEntity(String filePath, double insertX, double insertY) {
        this.filePath = filePath;
        this.insertX = insertX;
        this.insertY = insertY;
    }
}
