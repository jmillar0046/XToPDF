package com.xtopdf.xtopdf.entities;

/**
 * OLEFRAME/OLE2FRAME entity - Linked external OLE content.
 * DWG format: type=29, x, y, width, height, oleType, dataLength, oleData (variable)
 * DXF group codes: 10/20 (insertion point), 40/41 (width/height), 70 (OLE version), 1 (OLE data)
 * 
 * Note: OLE (Object Linking and Embedding) requires Windows-specific COM objects.
 * For PDF rendering, we display a placeholder indicating OLE content is present.
 */
public class OleFrameEntity extends DxfEntity {
    private double insertX;
    private double insertY;
    private double width = 100.0;
    private double height = 100.0;
    private int oleVersion = 1; // 1=OLEFRAME, 2=OLE2FRAME
    private String oleData = "";
    private String oleType = ""; // e.g., "Excel.Sheet", "Word.Document"
    
    public OleFrameEntity() {}
    
    public OleFrameEntity(double insertX, double insertY, double width, double height) {
        this.insertX = insertX;
        this.insertY = insertY;
        this.width = width;
        this.height = height;
    }
    
    public double getInsertX() { return insertX; }
    public void setInsertX(double insertX) { this.insertX = insertX; }
    
    public double getInsertY() { return insertY; }
    public void setInsertY(double insertY) { this.insertY = insertY; }
    
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
    
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    
    public int getOleVersion() { return oleVersion; }
    public void setOleVersion(int oleVersion) { this.oleVersion = oleVersion; }
    
    public String getOleData() { return oleData; }
    public void setOleData(String oleData) { this.oleData = oleData; }
    
    public String getOleType() { return oleType; }
    public void setOleType(String oleType) { this.oleType = oleType; }
}
