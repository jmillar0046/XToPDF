package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * INSERT entity - Placement of a BLOCK.
 * References a block definition and applies transformations (translate, scale, rotate).
 * 
 * DWG format: type=15, nameLength, name, x, y, scaleX, scaleY, rotation (variable)
 * DXF group codes: 2 (block name), 10/20 (insertion point), 41/42 (X/Y scale), 50 (rotation angle)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InsertEntity extends DxfEntity {
    private String blockName = "";
    private double insertX = 0.0;
    private double insertY = 0.0;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double rotation = 0.0; // in degrees
    
    public InsertEntity(String blockName, double insertX, double insertY) {
        this.blockName = blockName;
        this.insertX = insertX;
        this.insertY = insertY;
    }
}
