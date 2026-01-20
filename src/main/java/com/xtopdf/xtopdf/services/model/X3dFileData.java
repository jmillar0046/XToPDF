package com.xtopdf.xtopdf.services.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for X3D file metadata.
 */
public class X3dFileData {
    public String version = "Unknown";
    public int totalNodes = 0;
    public int shapeCount = 0;
    public int transformCount = 0;
    public int materialCount = 0;
    public int geometryCount = 0;
    public Map<String, Integer> nodeTypes = new HashMap<>();
}
