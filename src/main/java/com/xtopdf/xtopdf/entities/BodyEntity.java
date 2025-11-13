package com.xtopdf.xtopdf.entities;

/**
 * BODY entity - ACIS solid model data.
 * DWG format: type=24, dataLength, acisData...
 * DXF group codes: 1 (ACIS data), 70 (version)
 * 
 * Note: BODY entities contain ACIS geometric kernel data which requires
 * a full geometric modeling kernel to interpret and render.
 * For PDF rendering, we display a placeholder.
 */
public class BodyEntity extends DxfEntity {
    private String acisData = "";
    private int version = 1;
    
    public BodyEntity() {}
    
    public String getAcisData() { return acisData; }
    public void setAcisData(String acisData) { this.acisData = acisData; }
    
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
