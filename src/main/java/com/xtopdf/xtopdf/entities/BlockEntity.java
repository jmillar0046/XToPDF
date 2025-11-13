package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BlockEntity extends DxfEntity {
    private String name = "";
    private double baseX = 0.0;
    private double baseY = 0.0;
    private List<DxfEntity> entities = new ArrayList<>();
    
    public BlockEntity(String name) {
        this.name = name;
    }
    
    public void addEntity(DxfEntity entity) {
        entities.add(entity);
    }
}
