package com.xtopdf.xtopdf.entities;

/**
 * TOLERANCE entity - GD&T (Geometric Dimensioning and Tolerancing) annotation.
 * DWG format: type=12, x, y, height, text (3 doubles + string)
 * DXF group codes: 10/20 (insertion point), 40 (height), 1 (tolerance string)
 */
public class ToleranceEntity extends DxfEntity {
    private double x;
    private double y;
    private double height = 10.0;
    private String toleranceString = ""; // GD&T specification
    
    public ToleranceEntity() {}
    
    public ToleranceEntity(double x, double y, double height, String toleranceString) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.toleranceString = toleranceString;
    }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public String getToleranceString() { return toleranceString; }
    public void setToleranceString(String toleranceString) { this.toleranceString = toleranceString; }
}
