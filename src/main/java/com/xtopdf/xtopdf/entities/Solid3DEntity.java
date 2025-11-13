package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 3DSOLID entity - BREP (Boundary Representation) solid geometry.
 * DWG format: type=22, dataLength, binaryData...
 * DXF group codes: 1 (proprietary data), 70 (version)
 * 
 * Note: Full 3DSOLID rendering requires a 3D geometric engine (ACIS/Parasolid).
 * For PDF rendering, we display a placeholder indicating the solid is present.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Solid3DEntity extends DxfEntity {
    private String proprietaryData = "";
    private double boundingBoxMinX, boundingBoxMinY, boundingBoxMinZ;
    private double boundingBoxMaxX, boundingBoxMaxY, boundingBoxMaxZ;
}
