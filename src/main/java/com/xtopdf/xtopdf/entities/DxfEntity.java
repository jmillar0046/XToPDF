package com.xtopdf.xtopdf.entities;

/**
 * Base class for all DXF/DWG entities.
 */
public abstract class DxfEntity {
    private String layer = "0"; // Default layer
    
    public String getLayer() {
        return layer;
    }
    
    public void setLayer(String layer) {
        this.layer = layer;
    }
}
