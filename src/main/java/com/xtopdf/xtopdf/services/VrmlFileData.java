package com.xtopdf.xtopdf.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for VRML file metadata.
 */
class VrmlFileData {
    String version = "Unknown";
    int totalNodes = 0;
    int shapeCount = 0;
    int transformCount = 0;
    int materialCount = 0;
    Map<String, Integer> nodeTypes = new HashMap<>();
}
