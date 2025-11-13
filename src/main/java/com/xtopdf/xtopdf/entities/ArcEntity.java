package com.xtopdf.xtopdf.entities;

/**
 * ARC entity - Circular arc (portion of circle).
 * DWG format: type=3, centerX, centerY, radius, startAngle, endAngle (5 doubles)
 * DXF group codes: 10/20 (center XY), 40 (radius), 50 (start angle), 51 (end angle)
 */
public class ArcEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double radius;
    private double startAngle; // in degrees
    private double endAngle;   // in degrees
    
    public ArcEntity() {}
    
    public ArcEntity(double centerX, double centerY, double radius, double startAngle, double endAngle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
    }
    
    public double getCenterX() { return centerX; }
    public void setCenterX(double centerX) { this.centerX = centerX; }
    
    public double getCenterY() { return centerY; }
    public void setCenterY(double centerY) { this.centerY = centerY; }
    
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    
    public double getStartAngle() { return startAngle; }
    public void setStartAngle(double startAngle) { this.startAngle = startAngle; }
    
    public double getEndAngle() { return endAngle; }
    public void setEndAngle(double endAngle) { this.endAngle = endAngle; }
}
