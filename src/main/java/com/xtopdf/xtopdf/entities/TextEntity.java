package com.xtopdf.xtopdf.entities;

/**
 * TEXT entity - Single-line text.
 * DWG format: type=8, x, y, height, text (2 doubles + 1 double + string)
 * DXF group codes: 10/20 (insertion point), 40 (height), 1 (text value), 50 (rotation angle)
 */
public class TextEntity extends DxfEntity {
    private double x;
    private double y;
    private double height = 10.0; // Default text height
    private String text = "";
    private double rotationAngle = 0.0; // in degrees
    
    public TextEntity() {}
    
    public TextEntity(double x, double y, double height, String text) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.text = text;
    }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public double getRotationAngle() { return rotationAngle; }
    public void setRotationAngle(double rotationAngle) { this.rotationAngle = rotationAngle; }
}
