package com.xtopdf.xtopdf.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class WatermarkService {
    
    private static final float DEFAULT_OPACITY = 0.3f;
    
    public void addWatermark(File pdfFile, WatermarkConfig config) throws IOException {
        if (!config.isEnabled() || config.getText() == null || config.getText().trim().isEmpty()) {
            return; // Watermark not enabled or no text provided
        }
        
        // Create a temporary file for the modified PDF
        File tempFile = File.createTempFile("temp_", ".pdf");
        
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(pdfFile), new PdfWriter(tempFile))) {
            int numberOfPages = pdfDoc.getNumberOfPages();
            
            for (int i = 1; i <= numberOfPages; i++) {
                var page = pdfDoc.getPage(i);
                Rectangle pageSize = page.getPageSize();
                
                // Create canvas based on layer (foreground or background)
                PdfCanvas pdfCanvas;
                if (config.getLayer() == WatermarkLayer.BACKGROUND) {
                    // Add watermark under the content
                    pdfCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
                } else {
                    // Add watermark over the content
                    pdfCanvas = new PdfCanvas(page);
                }
                
                // Set transparency
                PdfExtGState gs1 = new PdfExtGState();
                gs1.setFillOpacity(DEFAULT_OPACITY);
                pdfCanvas.setExtGState(gs1);
                
                Canvas canvas = new Canvas(pdfCanvas, pageSize);
                
                // Create paragraph with watermark text
                Paragraph paragraph = new Paragraph(config.getText())
                        .setFontSize(config.getFontSize())
                        .setTextAlignment(TextAlignment.CENTER);
                
                // Calculate position (center of page)
                float centerX = pageSize.getWidth() / 2;
                float centerY = pageSize.getHeight() / 2;
                
                // Apply rotation based on orientation
                float rotationAngle = getRotationAngle(config.getOrientation(), pageSize);
                
                // Position and rotate the watermark
                paragraph.setFixedPosition(0, 0, pageSize.getWidth());
                paragraph.setRotationAngle(Math.toRadians(rotationAngle));
                
                // Adjust position based on orientation
                if (config.getOrientation() == WatermarkOrientation.VERTICAL) {
                    // For vertical text, we need to adjust positioning
                    canvas.showTextAligned(paragraph, centerX, centerY, i, 
                            TextAlignment.CENTER, com.itextpdf.layout.properties.VerticalAlignment.MIDDLE, 
                            (float) Math.toRadians(rotationAngle));
                } else {
                    canvas.showTextAligned(paragraph, centerX, centerY, i, 
                            TextAlignment.CENTER, com.itextpdf.layout.properties.VerticalAlignment.MIDDLE, 
                            (float) Math.toRadians(rotationAngle));
                }
                
                canvas.close();
            }
        }
        
        // Replace original file with the modified one
        if (pdfFile.delete()) {
            if (!tempFile.renameTo(pdfFile)) {
                throw new IOException("Failed to replace original PDF with watermarked version");
            }
        } else {
            tempFile.delete();
            throw new IOException("Failed to delete original PDF");
        }
    }
    
    private float getRotationAngle(WatermarkOrientation orientation, Rectangle pageSize) {
        return switch (orientation) {
            case HORIZONTAL -> 0f;
            case VERTICAL -> 90f;
            case DIAGONAL_UP -> {
                // Calculate angle from upper-left to bottom-right
                float angle = (float) Math.toDegrees(Math.atan2(
                    -pageSize.getHeight(), pageSize.getWidth()
                ));
                yield angle;
            }
            case DIAGONAL_DOWN -> {
                // Calculate angle from bottom-left to top-right
                float angle = (float) Math.toDegrees(Math.atan2(
                    pageSize.getHeight(), pageSize.getWidth()
                ));
                yield angle;
            }
        };
    }
}
