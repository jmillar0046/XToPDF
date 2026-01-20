package com.xtopdf.xtopdf.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for X3D file metadata.
 */
class X3dFileData {
    String version = "Unknown";
    int totalNodes = 0;
    int shapeCount = 0;
    int transformCount = 0;
    int materialCount = 0;
    int geometryCount = 0;
    Map<String, Integer> nodeTypes = new HashMap<>();
}
