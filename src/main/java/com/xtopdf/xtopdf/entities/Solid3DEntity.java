package com.xtopdf.xtopdf.entities;

/**
 * 3DSOLID entity - BREP (Boundary Representation) solid geometry.
 * DWG format: type=22, dataLength, binaryData...
 * DXF group codes: 1 (proprietary data), 70 (version)
 * 
 * Note: Full 3DSOLID rendering requires a 3D geometric engine (ACIS/Parasolid).
 * For PDF rendering, we display a placeholder indicating the solid is present.
 */
public class Solid3DEntity extends DxfEntity {
    private String proprietaryData = "";
    private double boundingBoxMinX, boundingBoxMinY, boundingBoxMinZ;
    private double boundingBoxMaxX, boundingBoxMaxY, boundingBoxMaxZ;
    
    public Solid3DEntity() {}
    
    public String getProprietaryData() { return proprietaryData; }
    public void setProprietaryData(String proprietaryData) { this.proprietaryData = proprietaryData; }
    
    public double getBoundingBoxMinX() { return boundingBoxMinX; }
    public void setBoundingBoxMinX(double boundingBoxMinX) { this.boundingBoxMinX = boundingBoxMinX; }
    
    public double getBoundingBoxMinY() { return boundingBoxMinY; }
    public void setBoundingBoxMinY(double boundingBoxMinY) { this.boundingBoxMinY = boundingBoxMinY; }
    
    public double getBoundingBoxMinZ() { return boundingBoxMinZ; }
    public void setBoundingBoxMinZ(double boundingBoxMinZ) { this.boundingBoxMinZ = boundingBoxMinZ; }
    
    public double getBoundingBoxMaxX() { return boundingBoxMaxX; }
    public void setBoundingBoxMaxX(double boundingBoxMaxX) { this.boundingBoxMaxX = boundingBoxMaxX; }
    
    public double getBoundingBoxMaxY() { return boundingBoxMaxY; }
    public void setBoundingBoxMaxY(double boundingBoxMaxY) { this.boundingBoxMaxY = boundingBoxMaxY; }
    
    public double getBoundingBoxMaxZ() { return boundingBoxMaxZ; }
    public void setBoundingBoxMaxZ(double boundingBoxMaxZ) { this.boundingBoxMaxZ = boundingBoxMaxZ; }
}
