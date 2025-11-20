package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.entities.*;

import java.util.List;

/**
 * Utility class for calculating scales and coordinate transformations for DXF entities.
 */
public class DxfCoordinateCalculator {
    
    /**
     * Calculate scale factor to fit entities on a page with margins.
     * 
     * @param entities List of entities to analyze
     * @param pageWidth Width of the page in points
     * @param pageHeight Height of the page in points
     * @return Scale factor to fit entities on page
     */
    public double calculateScale(List<DxfEntity> entities, double pageWidth, double pageHeight) {
        if (entities.isEmpty()) {
            return 1.0;
        }
        
        // Find bounding box
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        
        for (DxfEntity entity : entities) {
            if (entity instanceof LineEntity) {
                LineEntity line = (LineEntity) entity;
                minX = Math.min(minX, Math.min(line.getX1(), line.getX2()));
                minY = Math.min(minY, Math.min(line.getY1(), line.getY2()));
                maxX = Math.max(maxX, Math.max(line.getX1(), line.getX2()));
                maxY = Math.max(maxY, Math.max(line.getY1(), line.getY2()));
            } else if (entity instanceof CircleEntity) {
                CircleEntity circle = (CircleEntity) entity;
                minX = Math.min(minX, circle.getCenterX() - circle.getRadius());
                minY = Math.min(minY, circle.getCenterY() - circle.getRadius());
                maxX = Math.max(maxX, circle.getCenterX() + circle.getRadius());
                maxY = Math.max(maxY, circle.getCenterY() + circle.getRadius());
            } else if (entity instanceof ArcEntity) {
                ArcEntity arc = (ArcEntity) entity;
                minX = Math.min(minX, arc.getCenterX() - arc.getRadius());
                minY = Math.min(minY, arc.getCenterY() - arc.getRadius());
                maxX = Math.max(maxX, arc.getCenterX() + arc.getRadius());
                maxY = Math.max(maxY, arc.getCenterY() + arc.getRadius());
            } else if (entity instanceof PointEntity) {
                PointEntity point = (PointEntity) entity;
                minX = Math.min(minX, point.getX());
                minY = Math.min(minY, point.getY());
                maxX = Math.max(maxX, point.getX());
                maxY = Math.max(maxY, point.getY());
            }
            // Add other entity types for bounding box calculation
        }
        
        double width = maxX - minX;
        double height = maxY - minY;
        
        if (width <= 0 || height <= 0) {
            return 1.0;
        }
        
        // Calculate scale to fit on page with margins
        double availableWidth = pageWidth - 100;
        double availableHeight = pageHeight - 100;
        
        double scaleX = availableWidth / width;
        double scaleY = availableHeight / height;
        
        return Math.min(scaleX, scaleY);
    }
}
