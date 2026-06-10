package com.xtopdf.xtopdf.services.conversion.threed;

/**
 * Immutable record representing the 3D bounding box extents of a model.
 */
public record BoundingBox3D(
        float minX, float maxX,
        float minY, float maxY,
        float minZ, float maxZ
) {
    public float width() { return maxX - minX; }
    public float height() { return maxY - minY; }
    public float depth() { return maxZ - minZ; }

    public BoundingBox3D expand(float x, float y, float z) {
        return new BoundingBox3D(
                Math.min(minX, x), Math.max(maxX, x),
                Math.min(minY, y), Math.max(maxY, y),
                Math.min(minZ, z), Math.max(maxZ, z)
        );
    }

    public static BoundingBox3D initial(float x, float y, float z) {
        return new BoundingBox3D(x, x, y, y, z, z);
    }
}
