package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for rendering DXF entities to PDF using the abstraction layer.
 * Provides methods that mirror canvas operations for easier migration.
 */
public class DxfPdfRenderer {
    
    private final PdfDocumentBuilder builder;
    private double currentX;
    private double currentY;
    private List<Double> pathX = new ArrayList<>();
    private List<Double> pathY = new ArrayList<>();
    private boolean pathStarted = false;
    
    public DxfPdfRenderer(PdfDocumentBuilder builder) {
        this.builder = builder;
    }
    
    public void moveTo(double x, double y) {
        if (!pathStarted) {
            pathX.clear();
            pathY.clear();
            pathStarted = true;
        }
        pathX.add(x);
        pathY.add(y);
        currentX = x;
        currentY = y;
    }
    
    public void lineTo(double x, double y) {
        if (pathStarted) {
            pathX.add(x);
            pathY.add(y);
        } else {
            moveTo(x, y);
        }
        currentX = x;
        currentY = y;
    }
    
    public void closePath() {
        if (pathStarted && !pathX.isEmpty()) {
            pathX.add(pathX.get(0));
            pathY.add(pathY.get(0));
        }
    }
    
    public void stroke() throws IOException {
        if (pathStarted && pathX.size() >= 2) {
            // Draw lines between consecutive points
            for (int i = 0; i < pathX.size() - 1; i++) {
                builder.drawLine(
                    pathX.get(i).floatValue(), 
                    pathY.get(i).floatValue(),
                    pathX.get(i + 1).floatValue(), 
                    pathY.get(i + 1).floatValue()
                );
            }
        }
        pathX.clear();
        pathY.clear();
        pathStarted = false;
    }
    
    public void fill() throws IOException {
        if (pathStarted && pathX.size() >= 3) {
            float[] xPoints = new float[pathX.size()];
            float[] yPoints = new float[pathY.size()];
            for (int i = 0; i < pathX.size(); i++) {
                xPoints[i] = pathX.get(i).floatValue();
                yPoints[i] = pathY.get(i).floatValue();
            }
            builder.drawPolygon(xPoints, yPoints, xPoints.length, true);
        }
        pathX.clear();
        pathY.clear();
        pathStarted = false;
    }
    
    public void fillStroke() throws IOException {
        if (pathStarted && pathX.size() >= 3) {
            float[] xPoints = new float[pathX.size()];
            float[] yPoints = new float[pathY.size()];
            for (int i = 0; i < pathX.size(); i++) {
                xPoints[i] = pathX.get(i).floatValue();
                yPoints[i] = pathY.get(i).floatValue();
            }
            builder.drawPolygon(xPoints, yPoints, xPoints.length, true);
        }
        pathX.clear();
        pathY.clear();
        pathStarted = false;
    }
    
    public void circle(double x, double y, double radius) throws IOException {
        builder.drawCircle((float)x, (float)y, (float)radius);
    }
    
    public void arc(double x1, double y1, double x2, double y2, double startAngle, double sweepAngle) throws IOException {
        double width = x2 - x1;
        double height = y2 - y1;
        double centerX = x1 + width / 2;
        double centerY = y1 + height / 2;
        double radius = Math.max(width, height) / 2;
        builder.drawArc((float)centerX, (float)centerY, (float)radius, 
                       (float)startAngle, (float)sweepAngle);
    }
    
    public void ellipse(double x1, double y1, double x2, double y2) throws IOException {
        double width = x2 - x1;
        double height = y2 - y1;
        double centerX = x1 + width / 2;
        double centerY = y1 + height / 2;
        double radiusX = width / 2;
        double radiusY = height / 2;
        builder.drawEllipse((float)centerX, (float)centerY, (float)radiusX, (float)radiusY);
    }
    
    public void rectangle(double x, double y, double width, double height) throws IOException {
        moveTo(x, y);
        lineTo(x + width, y);
        lineTo(x + width, y + height);
        lineTo(x, y + height);
        closePath();
        stroke();
    }
    
    public void setStrokeColor(float r, float g, float b) throws IOException {
        builder.setStrokeColor((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }
    
    public void setFillColor(float r, float g, float b) throws IOException {
        builder.setFillColor((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }
    
    public void setLineWidth(float width) throws IOException {
        builder.setLineWidth(width);
    }
    
    public void setLineDash(float dashLength, float gapLength) throws IOException {
        builder.setLineDash(dashLength, gapLength);
    }
    
    public void saveState() throws IOException {
        builder.saveState();
    }
    
    public void restoreState() throws IOException {
        builder.restoreState();
    }
    
    public void addText(double x, double y, String text, float fontSize) throws IOException {
        // Position text at the specified location
        builder.setFontSize(fontSize); // If PdfDocumentBuilder supports this
        builder.addText(x, y, text);
    }
}
