package com.xtopdf.xtopdf.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data holder for 3MF file metadata.
 */
class ThreeMfFileData {
    int objectCount = 0;
    int meshCount = 0;
    int vertexCount = 0;
    int triangleCount = 0;
    List<String> files = new ArrayList<>();
    Set<String> components = new HashSet<>();
}
