package com.xtopdf.xtopdf.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import com.xtopdf.xtopdf.entities.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service to convert DXF (Drawing Exchange Format) files to PDF.
 * 
 * This implementation parses DXF entities and renders them as actual graphics
 * in the PDF using PDFBox.
 * 
 * Supported entities: LINE, CIRCLE, ARC, ELLIPSE, POINT, POLYLINE, SOLID/TRACE, and many more.
 */
@RequiredArgsConstructor
@Service
public class DxfToPdfService {
    
    private final PdfBackendProvider pdfBackend;
    private final DxfEntityParser parser;
    private final DxfCoordinateCalculator calculator;
    
    /**
     * Convert DXF file to PDF.
     * 
     * @param dxfFile Input DXF file
     * @param pdfFile Output PDF file
     * @throws IOException if conversion fails
     */
    public void convertDxfToPdf(MultipartFile dxfFile, File pdfFile) throws IOException {
        // Parse DXF entities and blocks
        List<DxfEntity> entities = parser.parseDxfEntities(dxfFile);
        
        // Create PDF using PDFBox abstraction
        try (PdfDocumentBuilder builder = pdfBackend.createBuilder()) {
            // Calculate scale factor to fit drawing on page (A4 = 595x842 points)
            double scale = calculator.calculateScale(entities, 595, 842);
            double offsetX = 50; // Left margin
            double offsetY = 50; // Bottom margin
            
            // Create DxfPdfRenderer helper for canvas-like operations
            DxfPdfRenderer renderer = new DxfPdfRenderer(builder);
            
            // Set up drawing parameters
            renderer.setStrokeColor(0, 0, 0); // Black
            renderer.setFillColor(0.827f, 0.827f, 0.827f); // Light gray
            renderer.setLineWidth(1);
            
            // Create renderer with block registry
            DxfEntityRenderer entityRenderer = new DxfEntityRenderer(parser.getBlockRegistry());
            
            // Render each entity (blocks are stored in registry, not rendered directly)
            for (DxfEntity entity : entities) {
                if (!(entity instanceof BlockEntity)) {
                    entityRenderer.renderEntity(renderer, entity, scale, offsetX, offsetY, 1.0, 1.0, 0.0);
                }
            }
            
            builder.save(pdfFile);
        } catch (Exception e) {
            throw new IOException("Error creating PDF from DXF: " + e.getMessage(), e);
        }
    }
}
