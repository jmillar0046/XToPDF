package com.xtopdf.xtopdf.entities;

/**
 * VIEWPORT entity - Defines visible area in paper space/layouts.
 * DWG format: type=26, x, y, width, height, scale (5 doubles)
 * DXF group codes: 10/20 (center), 40/41 (width/height), 45 (view scale)
 * 
 * For PDF rendering, we display as a clipping rectangle outline.
 */
public class ViewportEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double width = 100.0;
    private double height = 100.0;
    private double scale = 1.0;
    
    public ViewportEntity() {}
    
    public ViewportEntity(double centerX, double centerY, double width, double height) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
    }
    
    public double getCenterX() { return centerX; }
    public void setCenterX(double centerX) { this.centerX = centerX; }
    
    public double getCenterY() { return centerY; }
    public void setCenterY(double centerY) { this.centerY = centerY; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }
}
