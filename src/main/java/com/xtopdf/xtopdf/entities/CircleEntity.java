package com.xtopdf.xtopdf.entities;

/**
 * CIRCLE entity - Circle defined by center + radius.
 * DWG format: type=2, centerX, centerY, radius (3 doubles)
 * DXF group codes: 10/20 (center XY), 40 (radius)
 */
public class CircleEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double radius;
    
    public CircleEntity() {}
    
    public CircleEntity(double centerX, double centerY, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }
    
    public double getCenterX() { return centerX; }
    public void setCenterX(double centerX) { this.centerX = centerX; }
    
    public double getCenterY() { return centerY; }
    public void setCenterY(double centerY) { this.centerY = centerY; }
    
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
}
