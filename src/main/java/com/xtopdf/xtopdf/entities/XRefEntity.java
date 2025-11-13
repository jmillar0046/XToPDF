package com.xtopdf.xtopdf.entities;

/**
 * XREF entity - External reference (another DWG file).
 * References an external DWG file that should be loaded and rendered.
 * 
 * DWG format: type=17, pathLength, path, x, y (variable)
 * DXF group codes: 1 (file path), 10/20 (insertion point)
 * 
 * Note: For security and simplicity, this implementation documents the XREF
 * but does not automatically load external files. Users should manually include
 * referenced content or use specialized CAD tools.
 */
public class XRefEntity extends DxfEntity {
    private String filePath = "";
    private double insertX = 0.0;
    private double insertY = 0.0;
    
    public XRefEntity() {}
    
    public XRefEntity(String filePath, double insertX, double insertY) {
        this.filePath = filePath;
        this.insertX = insertX;
        this.insertY = insertY;
    }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public double getInsertX() { return insertX; }
    public void setInsertX(double insertX) { this.insertX = insertX; }
    
    public double getInsertY() { return insertY; }
    public void setInsertY(double insertY) { this.insertY = insertY; }
}
