package com.xtopdf.xtopdf.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * TABLE entity - Tabular data with grid and text cells.
 * DWG format: type=13, x, y, rows, cols, cellHeight, cellWidth, followed by cell text values
 * DXF group codes: 10/20 (insertion point), 90/91 (rows/cols), 40/41 (cell dimensions), 1 (cell text)
 */
public class TableEntity extends DxfEntity {
    private double x;
    private double y;
    private int rows;
    private int columns;
    private double cellHeight = 10.0;
    private double cellWidth = 50.0;
    private List<String> cellValues = new ArrayList<>(); // Stored row by row
    
    public TableEntity() {}
    
    public TableEntity(double x, double y, int rows, int columns, double cellHeight, double cellWidth) {
        this.x = x;
        this.y = y;
        this.rows = rows;
        this.columns = columns;
        this.cellHeight = cellHeight;
        this.cellWidth = cellWidth;
    }
    
    public void addCellValue(String value) {
        cellValues.add(value);
    }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    
    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }
    
    public double getCellHeight() { return cellHeight; }
    public void setCellHeight(double cellHeight) { this.cellHeight = cellHeight; }
    
    public double getCellWidth() { return cellWidth; }
    public void setCellWidth(double cellWidth) { this.cellWidth = cellWidth; }
    
    public List<String> getCellValues() { return cellValues; }
    public void setCellValues(List<String> cellValues) { this.cellValues = cellValues; }
}
