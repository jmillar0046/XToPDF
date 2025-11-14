package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * UNDERLAY entity - PDF/DGN/DWF reference underlay.
 * DWG format: type=28, x, y, scale, rotation, pathLength, underlayPath (variable)
 * DXF group codes: 10/20 (insertion point), 41/42/43 (scale), 50 (rotation), 1 (file path)
 * 
 * Note: Full underlay rendering requires loading external PDF/DGN/DWF files.
 * For PDF rendering, we display a placeholder indicating the underlay reference.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UnderlayEntity extends DxfEntity {
    private double insertX;
    private double insertY;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;
    private double rotation = 0.0; // in degrees
    private String underlayPath = "";
    private String underlayType = "PDF"; // PDF, DGN, or DWF
    
    public UnderlayEntity(double insertX, double insertY, String underlayPath) {
        this.insertX = insertX;
        this.insertY = insertY;
        this.underlayPath = underlayPath;
    }
}
