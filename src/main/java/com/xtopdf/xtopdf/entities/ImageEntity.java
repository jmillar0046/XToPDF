package com.xtopdf.xtopdf.entities;

/**
 * IMAGE entity - Embedded raster image.
 * DWG format: type=27, x, y, width, height, pathLength, imagePath (variable)
 * DXF group codes: 10/20 (insertion point), 13/23 (U vector), 14/24 (V vector), 1 (image path)
 * 
 * For PDF rendering, we display a placeholder rectangle with the image filename.
 * Full image rendering would require loading and embedding the actual image file.
 */
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
    
    public ImageEntity() {}
    
    public ImageEntity(double insertX, double insertY, double width, double height, String imagePath) {
        this.insertX = insertX;
        this.insertY = insertY;
        this.width = width;
        this.height = height;
        this.imagePath = imagePath;
    }
    
    public double getInsertX() { return insertX; }
    public void setInsertX(double insertX) { this.insertX = insertX; }
    
    public double getInsertY() { return insertY; }
    public void setInsertY(double insertY) { this.insertY = insertY; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    
    public double getUVectorX() { return uVectorX; }
    public void setUVectorX(double uVectorX) { this.uVectorX = uVectorX; }
    
    public double getUVectorY() { return uVectorY; }
    public void setUVectorY(double uVectorY) { this.uVectorY = uVectorY; }
    
    public double getVVectorX() { return vVectorX; }
    public void setVVectorX(double vVectorX) { this.vVectorX = vVectorX; }
    
    public double getVVectorY() { return vVectorY; }
    public void setVVectorY(double vVectorY) { this.vVectorY = vVectorY; }
}
