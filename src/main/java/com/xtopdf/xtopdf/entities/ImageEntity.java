package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * IMAGE entity - Embedded raster image.
 * DWG format: type=27, x, y, width, height, pathLength, imagePath (variable)
 * DXF group codes: 10/20 (insertion point), 13/23 (U vector), 14/24 (V vector), 1 (image path)
 * 
 * For PDF rendering, we display a placeholder rectangle with the image filename.
 * Full image rendering would require loading and embedding the actual image file.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImageEntity extends DxfEntity {
    private double insertX;
    private double insertY;
    private double width = 100.0;
    private double height = 100.0;
    private String imagePath = "";
    private double uVectorX = 1.0; // U vector defines image orientation
    private double uVectorY = 0.0;
    private double vVectorX = 0.0;
    private double vVectorY = 1.0;
    
    public ImageEntity(double insertX, double insertY, double width, double height, String imagePath) {
        this.insertX = insertX;
        this.insertY = insertY;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
    }
}
