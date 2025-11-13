package com.xtopdf.xtopdf.entities;

/**
 * ELLIPSE entity - Ellipse (center, major/minor axis, start/end angle).
 * DWG format: type=6, centerX, centerY, majorAxisX, majorAxisY, ratio, startAngle, endAngle (7 doubles)
 * DXF group codes: 10/20 (center), 11/21 (major axis endpoint), 40 (ratio of minor to major axis), 41/42 (start/end parameters)
 */
public class EllipseEntity extends DxfEntity {
    private double centerX;
    private double centerY;
    private double majorAxisX; // Major axis endpoint relative to center
    private double majorAxisY;
    private double ratio;      // Ratio of minor axis to major axis
    private double startParam = 0;  // Start parameter (0 to 2*PI)
    private double endParam = Math.PI * 2; // End parameter
    
    public EllipseEntity() {}
    
    public EllipseEntity(double centerX, double centerY, double majorAxisX, double majorAxisY, double ratio) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.majorAxisX = majorAxisX;
        this.majorAxisY = majorAxisY;
        this.ratio = ratio;
    }
    
    public double getCenterX() { return centerX; }
    public void setCenterX(double centerX) { this.centerX = centerX; }
    
    public double getCenterY() { return centerY; }
    public void setCenterY(double centerY) { this.centerY = centerY; }
    
    public double getMajorAxisX() { return majorAxisX; }
    public void setMajorAxisX(double majorAxisX) { this.majorAxisX = majorAxisX; }
    
    public double getMajorAxisY() { return majorAxisY; }
    public void setMajorAxisY(double majorAxisY) { this.majorAxisY = majorAxisY; }
    
    public double getRatio() { return ratio; }
    public void setRatio(double ratio) { this.ratio = ratio; }
    
    public double getStartParam() { return startParam; }
    public void setStartParam(double startParam) { this.startParam = startParam; }
    
    public double getEndParam() { return endParam; }
    public void setEndParam(double endParam) { this.endParam = endParam; }
}
