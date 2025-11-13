package com.xtopdf.xtopdf.entities;

/**
 * SURFACE entity - NURBS (Non-Uniform Rational B-Spline) surface.
 * DWG format: type=23, uDegree, vDegree, numUControlPoints, numVControlPoints, controlPoints...
 * DXF group codes: 71/72 (degree), 73/74 (control point counts)
 * 
 * Note: Full NURBS surface rendering requires advanced surface tessellation.
 * For PDF rendering, we display a wireframe approximation or placeholder.
 */
public class SurfaceEntity extends DxfEntity {
    private int uDegree = 3;
    private int vDegree = 3;
    private int numUControlPoints = 0;
    private int numVControlPoints = 0;
    private String surfaceData = ""; // Simplified representation
    
    public SurfaceEntity() {}
    
    public int getUDegree() { return uDegree; }
    public void setUDegree(int uDegree) { this.uDegree = uDegree; }
    
    public int getVDegree() { return vDegree; }
    public void setVDegree(int vDegree) { this.vDegree = vDegree; }
    
    public int getNumUControlPoints() { return numUControlPoints; }
    public void setNumUControlPoints(int numUControlPoints) { this.numUControlPoints = numUControlPoints; }
    
    public int getNumVControlPoints() { return numVControlPoints; }
    public void setNumVControlPoints(int numVControlPoints) { this.numVControlPoints = numVControlPoints; }
    
    public String getSurfaceData() { return surfaceData; }
    public void setSurfaceData(String surfaceData) { this.surfaceData = surfaceData; }
}
