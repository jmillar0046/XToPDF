package com.xtopdf.xtopdf.entities;

/**
 * SOLID/TRACE entity - Filled triangle or quadrilateral.
 * DWG format: type=7, x1, y1, x2, y2, x3, y3, x4, y4 (8 doubles, x4/y4 optional if triangle)
 * DXF group codes: 10/20, 11/21, 12/22, 13/23 for four corners
 */
public class SolidEntity extends DxfEntity {
    private double x1, y1;
    private double x2, y2;
    private double x3, y3;
    private double x4, y4;
    private boolean isTriangle = false;
    
    public SolidEntity() {}
    
    public SolidEntity(double x1, double y1, double x2, double y2, double x3, double y3) {
        this(x1, y1, x2, y2, x3, y3, x3, y3);
        this.isTriangle = true;
    }
    
    public SolidEntity(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.x3 = x3; this.y3 = y3;
        this.x4 = x4; this.y4 = y4;
    }
    
    // Getters and setters
    public double getX1() { return x1; }
    public void setX1(double x1) { this.x1 = x1; }
    public double getY1() { return y1; }
    public void setY1(double y1) { this.y1 = y1; }
    
    public double getX2() { return x2; }
    public void setX2(double x2) { this.x2 = x2; }
    public double getY2() { return y2; }
    public void setY2(double y2) { this.y2 = y2; }
    
    public double getX3() { return x3; }
    public void setX3(double x3) { this.x3 = x3; }
    public double getY3() { return y3; }
    public void setY3(double y3) { this.y3 = y3; }
    
    public double getX4() { return x4; }
    public void setX4(double x4) { this.x4 = x4; }
    public double getY4() { return y4; }
    public void setY4(double y4) { this.y4 = y4; }
    
    public boolean isTriangle() { return isTriangle; }
    public void setTriangle(boolean triangle) { isTriangle = triangle; }
}
