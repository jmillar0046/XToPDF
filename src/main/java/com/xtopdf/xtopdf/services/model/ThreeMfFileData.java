package com.xtopdf.xtopdf.services.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data holder for 3MF file metadata.
 */
public class ThreeMfFileData {
    public int objectCount = 0;
    public int meshCount = 0;
    public int vertexCount = 0;
    public int triangleCount = 0;
    public List<String> files = new ArrayList<>();
    public Set<String> components = new HashSet<>();
}
