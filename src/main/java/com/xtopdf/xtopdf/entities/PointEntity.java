package com.xtopdf.xtopdf.entities;

/**
 * POINT entity - Single coordinate marker.
 * DWG format: type=4, x, y (2 doubles)
 * DXF group codes: 10/20 (XY position)
 */
public class PointEntity extends DxfEntity {
    private double x;
    private double y;
    
    public PointEntity() {}
    
    public PointEntity(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
}
