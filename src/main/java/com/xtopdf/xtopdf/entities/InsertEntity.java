package com.xtopdf.xtopdf.entities;

/**
 * INSERT entity - Placement of a BLOCK.
 * References a block definition and applies transformations (translate, scale, rotate).
 * 
 * DWG format: type=15, nameLength, name, x, y, scaleX, scaleY, rotation (variable)
 * DXF group codes: 2 (block name), 10/20 (insertion point), 41/42 (X/Y scale), 50 (rotation angle)
 */
public class InsertEntity extends DxfEntity {
    private String blockName = "";
    private double insertX = 0.0;
    private double insertY = 0.0;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double rotation = 0.0; // in degrees
    
    public InsertEntity() {}
    
    public InsertEntity(String blockName, double insertX, double insertY) {
        this.blockName = blockName;
        this.insertX = insertX;
        this.insertY = insertY;
    }
    
    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }
    
    public double getInsertX() { return insertX; }
    public void setInsertX(double insertX) { this.insertX = insertX; }
    
    public double getInsertY() { return insertY; }
    public void setInsertY(double insertY) { this.insertY = insertY; }
    
    public double getScaleX() { return scaleX; }
    public void setScaleX(double scaleX) { this.scaleX = scaleX; }
    
    public double getScaleY() { return scaleY; }
    public void setScaleY(double scaleY) { this.scaleY = scaleY; }
    
    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }
}
