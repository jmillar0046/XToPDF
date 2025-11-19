package com.xtopdf.xtopdf.pdf;

import java.io.File;
import java.io.IOException;

/**
 * Abstraction layer for PDF document generation.
 * This interface provides a backend-agnostic API for creating PDF documents,
 * allowing the underlying PDF library to be swapped without changing client code.
 * 
 * <p>Implementations can use different PDF libraries (e.g., iText, PDFBox, OpenPDF)
 * while maintaining the same API for all services.</p>
 * 
 * @see PdfBackendProvider
 */
public interface PdfDocumentBuilder extends AutoCloseable {
    
    /**
     * Creates a new page in the PDF document.
     * The page size will be determined by the implementation (typically A4).
     */
    void newPage();
    
    /**
     * Adds text at a specific position on the current page.
     * 
     * @param text The text to add
     * @param x The x-coordinate (in points, from left)
     * @param y The y-coordinate (in points, from bottom)
     * @throws IOException if an I/O error occurs
     */
    void addText(String text, float x, float y) throws IOException;
    
    /**
     * Adds a paragraph of text to the current page.
     * The text will be wrapped and positioned automatically.
     * 
     * @param text The paragraph text
     * @throws IOException if an I/O error occurs
     */
    void addParagraph(String text) throws IOException;
    
    /**
     * Adds a table with the specified data to the current page.
     * 
     * @param data The table data (rows x columns)
     * @throws IOException if an I/O error occurs
     */
    void addTable(String[][] data) throws IOException;
    
    /**
     * Adds an image to the current page.
     * The image will be scaled to fit the page if necessary.
     * 
     * @param imageData The image data as a byte array
     * @throws IOException if an I/O error occurs
     */
    void addImage(byte[] imageData) throws IOException;
    
    /**
     * Draws a line on the current page.
     * 
     * @param x1 Starting x-coordinate
     * @param y1 Starting y-coordinate
     * @param x2 Ending x-coordinate
     * @param y2 Ending y-coordinate
     * @throws IOException if an I/O error occurs
     */
    void drawLine(float x1, float y1, float x2, float y2) throws IOException;
    
    /**
     * Draws a circle on the current page.
     * 
     * @param cx Center x-coordinate
     * @param cy Center y-coordinate
     * @param radius Circle radius
     * @throws IOException if an I/O error occurs
     */
    void drawCircle(float cx, float cy, float radius) throws IOException;
    
    /**
     * Draws a rectangle on the current page.
     * 
     * @param x Left x-coordinate
     * @param y Bottom y-coordinate
     * @param width Rectangle width
     * @param height Rectangle height
     * @throws IOException if an I/O error occurs
     */
    void drawRectangle(float x, float y, float width, float height) throws IOException;
    
    /**
     * Saves the PDF document to the specified file.
     * 
     * @param outputFile The file to save the PDF to
     * @throws IOException if an I/O error occurs
     */
    void save(File outputFile) throws IOException;
    
    /**
     * Closes the PDF document and releases resources.
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    void close() throws IOException;
}
