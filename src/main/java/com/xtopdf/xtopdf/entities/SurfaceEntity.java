package com.xtopdf.xtopdf.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SURFACE entity - NURBS (Non-Uniform Rational B-Spline) surface.
 * DWG format: type=23, uDegree, vDegree, numUControlPoints, numVControlPoints, controlPoints...
 * DXF group codes: 71/72 (degree), 73/74 (control point counts)
 * 
 * Note: Full NURBS surface rendering requires advanced surface tessellation.
 * For PDF rendering, we display a wireframe approximation or placeholder.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SurfaceEntity extends DxfEntity {
    private int uDegree = 3;
    private int vDegree = 3;
    private int numUControlPoints = 0;
    private int numVControlPoints = 0;
    private String surfaceData = ""; // Simplified representation
}
