package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * BLOCK entity - Definition of reusable geometry.
 * A block is a named collection of entities that can be inserted multiple times.
 * Blocks can contain other blocks (recursive structure).
 * 
 * DWG format: type=14, nameLength, name, numEntities, entities...
 * DXF group codes: 2 (block name), followed by entity definitions, terminated by ENDBLK
 */
public class BlockEntity extends DxfEntity {
    private String name = "";
    private double baseX = 0.0;
    private double baseY = 0.0;
    private List<DxfEntity> entities = new ArrayList<>();
    
    public BlockEntity() {}
    
    public BlockEntity(String name) {
        this.name = name;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getBaseX() { return baseX; }
    public void setBaseX(double baseX) { this.baseX = baseX; }
    
    public double getBaseY() { return baseY; }
    public void setBaseY(double baseY) { this.baseY = baseY; }
    
    public List<DxfEntity> getEntities() { return entities; }
    public void setEntities(List<DxfEntity> entities) { this.entities = entities; }
    
    public void addEntity(DxfEntity entity) {
        entities.add(entity);
    }
}
