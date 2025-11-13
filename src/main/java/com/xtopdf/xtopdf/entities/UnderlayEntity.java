package com.xtopdf.xtopdf.entities;

/**
 * UNDERLAY entity - PDF/DGN/DWF reference underlay.
 * DWG format: type=28, x, y, scale, rotation, pathLength, underlayPath (variable)
 * DXF group codes: 10/20 (insertion point), 41/42/43 (scale), 50 (rotation), 1 (file path)
 * 
 * Note: Full underlay rendering requires loading external PDF/DGN/DWF files.
 * For PDF rendering, we display a placeholder indicating the underlay reference.
 */
public class UnderlayEntity extends DxfEntity {
    private double insertX;
    private double insertY;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double scaleZ = 1.0;
    private double rotation = 0.0; // in degrees
    private String underlayPath = "";
    private String underlayType = "PDF"; // PDF, DGN, or DWF
    
    public UnderlayEntity() {}
    
    public UnderlayEntity(double insertX, double insertY, String underlayPath) {
        this.insertX = insertX;
        this.insertY = insertY;
        this.underlayPath = underlayPath;
    }
    
    public double getInsertX() { return insertX; }
    public void setInsertX(double insertX) { this.insertX = insertX; }
    
    public double getInsertY() { return insertY; }
    public void setInsertY(double insertY) { this.insertY = insertY; }
    
    public double getScaleX() { return scaleX; }
    public void setScaleX(double scaleX) { this.scaleX = scaleX; }
    
    public double getScaleY() { return scaleY; }
    public void setScaleY(double scaleY) { this.scaleY = scaleY; }
    
    public double getScaleZ() { return scaleZ; }
    public void setScaleZ(double scaleZ) { this.scaleZ = scaleZ; }
    
    public double getRotation() { return rotation; }
    public void setRotation(double rotation) { this.rotation = rotation; }
    
    public String getUnderlayPath() { return underlayPath; }
    public void setUnderlayPath(String underlayPath) { this.underlayPath = underlayPath; }
    
    public String getUnderlayType() { return underlayType; }
    public void setUnderlayType(String underlayType) { this.underlayType = underlayType; }
}
