package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * BODY entity - ACIS solid model data.
 * DWG format: type=24, dataLength, acisData...
 * DXF group codes: 1 (ACIS data), 70 (version)
 * 
 * Note: BODY entities contain ACIS geometric kernel data which requires
 * a full geometric modeling kernel to interpret and render.
 * For PDF rendering, we display a placeholder.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BodyEntity extends DxfEntity {
    private String acisData = "";
    private int version = 1;
}
