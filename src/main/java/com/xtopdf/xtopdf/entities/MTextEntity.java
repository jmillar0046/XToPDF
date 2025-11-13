package com.xtopdf.xtopdf.entities;

/**
 * MTEXT entity - Multi-line text (formatted).
 * DWG format: type=9, x, y, width, height, text (4 doubles + string)
 * DXF group codes: 10/20 (insertion point), 40 (initial height), 41 (width), 1 (text), 7 (text style)
 */
public class MTextEntity extends DxfEntity {
    private double x;
    private double y;
    private double width = 100.0;
    private double height = 10.0;
    private String text = "";
    
    public MTextEntity() {}
    
    public MTextEntity(double x, double y, double width, double height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
