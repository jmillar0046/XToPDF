package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * POLYFACE MESH entity - Network of 3D faces forming a mesh.
 * DWG format: type=20, numVertices, numFaces, vertices..., face indices...
 * DXF group codes: 71 (vertex count), 72 (face count), multiple 10/20/30 for vertices
 * 
 * For PDF rendering, we project to 2D by ignoring Z coordinates and render as wireframe.
 */
public class PolyfaceMeshEntity extends DxfEntity {
    private List<Double> vertices = new ArrayList<>(); // x1, y1, z1, x2, y2, z2, ...
    private List<Integer> faceIndices = new ArrayList<>(); // indices into vertices list
    
    public PolyfaceMeshEntity() {}
    
    public void addVertex(double x, double y, double z) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
    }
    
    public void addFaceIndex(int index) {
        faceIndices.add(index);
    }
    
    public List<Double> getVertices() { return vertices; }
    public void setVertices(List<Double> vertices) { this.vertices = vertices; }
    
    public List<Integer> getFaceIndices() { return faceIndices; }
    public void setFaceIndices(List<Integer> faceIndices) { this.faceIndices = faceIndices; }
    
    public int getVertexCount() { return vertices.size() / 3; }
}
