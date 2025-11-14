package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ATTDEF/ATTRIB entity - Text attribute in a block.
 * Attributes are variable text fields that can be different for each block insertion.
 * 
 * DWG format: type=16, tag, prompt, defaultValue, x, y, height (variable)
 * DXF group codes: 2 (tag), 3 (prompt), 1 (default value), 10/20 (position), 40 (height)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AttributeEntity extends DxfEntity {
    private String tag = "";
    private String prompt = "";
    private String value = "";
    private double x = 0.0;
    private double y = 0.0;
    private double height = 10.0;
    
    public AttributeEntity(String tag, String value, double x, double y, double height) {
        this.tag = tag;
        this.value = value;
        this.x = x;
        this.y = y;
        this.height = height;
    }
}
