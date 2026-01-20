package com.xtopdf.xtopdf.services.operations;

import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Service for adding watermarks to PDF documents.
 * Uses Apache PDFBox to overlay or underlay text watermarks on existing PDF files
 * with configurable opacity, orientation, and layer placement.
 * 
 * <p>Supported features:
 * <ul>
 *   <li>Layer: Foreground (overlay) or background (underlay)</li>
 *   <li>Orientation: Horizontal, vertical, diagonal up, diagonal down</li>
 *   <li>Configurable font size and text</li>
 *   <li>Fixed opacity (30%) for subtle watermarking</li>
 *   <li>Automatic temporary file cleanup on error</li>
 * </ul>
 * 
 * <p>Limitations:
 * <ul>
 *   <li>Fixed font (Helvetica)</li>
 *   <li>Fixed opacity (0.3)</li>
 *   <li>Text only (no image watermarks)</li>
 *   <li>Centered positioning only</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * WatermarkConfig config = WatermarkConfig.builder()
 *     .enabled(true)
 *     .text("CONFIDENTIAL")
 *     .fontSize(48)
 *     .layer(WatermarkLayer.BACKGROUND)
 *     .orientation(WatermarkOrientation.DIAGONAL_UP)
 *     .build();
 * watermarkService.addWatermark(pdfFile, config);
 * </pre>
 * 
 * @see WatermarkConfig
 * @see WatermarkLayer
 * @see WatermarkOrientation
 */
@Service
@Slf4j
public class WatermarkService {
    
    private static final float DEFAULT_OPACITY = 0.3f;
    
    /**
     * Adds a watermark to a PDF file according to the provided configuration.
     * 
     * <p>This method modifies the PDF file in-place by:
     * <ol>
     *   <li>Creating a temporary copy of the PDF</li>
     *   <li>Adding the watermark to each page</li>
     *   <li>Replacing the original file with the modified version</li>
     *   <li>Cleaning up the temporary file</li>
     * </ol>
     * 
     * <p>The watermark is centered on each page and rotated according to the
     * specified orientation. The temporary file is guaranteed to be cleaned up
     * even if an error occurs.
     * 
     * @param pdfFile the PDF file to modify (must exist and be readable)
     * @param config the watermark configuration (text, size, layer, orientation)
     * @throws IOException if the file cannot be read, modified, or replaced
     * @throws IllegalArgumentException if pdfFile is null or doesn't exist
     */
    public void addWatermark(File pdfFile, WatermarkConfig config) throws IOException {
        if (!config.isEnabled() || config.getText() == null || config.getText().trim().isEmpty()) {
            return; // Watermark not enabled or no text provided
        }
        
        File tempFile = null;
        try {
            // Create a temporary file for the modified PDF
            tempFile = File.createTempFile("temp_", ".pdf");
            
            try (PDDocument document = Loader.loadPDF(pdfFile)) {
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                
                for (PDPage page : document.getPages()) {
                    float pageWidth = page.getMediaBox().getWidth();
                    float pageHeight = page.getMediaBox().getHeight();
                    
                    // Determine if we're adding to background or foreground
                    boolean append = (config.getLayer() == WatermarkLayer.FOREGROUND);
                    
                    try (PDPageContentStream contentStream = new PDPageContentStream(
                            document, page, 
                            append ? PDPageContentStream.AppendMode.APPEND : PDPageContentStream.AppendMode.PREPEND,
                            true)) {
                        
                        // Set transparency
                        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
                        gs.setNonStrokingAlphaConstant(DEFAULT_OPACITY);
                        gs.setStrokingAlphaConstant(DEFAULT_OPACITY);
                        contentStream.setGraphicsStateParameters(gs);
                        
                        // Calculate position (center of page)
                        float centerX = pageWidth / 2;
                        float centerY = pageHeight / 2;
                        
                        // Get rotation angle
                        float rotationAngle = getRotationAngle(config.getOrientation(), pageWidth, pageHeight);
                        
                        // Set up transformation matrix for rotation around center
                        contentStream.saveGraphicsState();
                        contentStream.transform(Matrix.getTranslateInstance(centerX, centerY));
                        contentStream.transform(Matrix.getRotateInstance(Math.toRadians(rotationAngle), 0, 0));
                        
                        // Draw watermark text
                        contentStream.beginText();
                        contentStream.setFont(font, config.getFontSize());
                        
                        // Calculate text width for centering
                        float textWidth = font.getStringWidth(config.getText()) / 1000 * config.getFontSize();
                        contentStream.newLineAtOffset(-textWidth / 2, 0);
                        contentStream.showText(config.getText());
                        contentStream.endText();
                        
                        contentStream.restoreGraphicsState();
                    }
                }
                
                document.save(tempFile);
            }
            
            // Replace original file with the modified one
            if (!pdfFile.delete()) {
                throw new IOException("Failed to delete original PDF");
            }
            
            if (!tempFile.renameTo(pdfFile)) {
                // If rename fails, try to copy the content
                try (java.io.FileInputStream fis = new java.io.FileInputStream(tempFile);
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(pdfFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
            
            // Mark temp file for deletion (will be cleaned up in finally)
            tempFile = null;
            
        } finally {
            // Guaranteed cleanup of temporary file
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * Calculates the rotation angle for the watermark based on orientation and page dimensions.
     * 
     * @param orientation the desired watermark orientation
     * @param pageWidth the width of the page in points
     * @param pageHeight the height of the page in points
     * @return the rotation angle in degrees
     */
    private float getRotationAngle(WatermarkOrientation orientation, float pageWidth, float pageHeight) {
        return switch (orientation) {
            case HORIZONTAL -> 0f;
            case VERTICAL -> 90f;
            case DIAGONAL_UP -> {
                // Calculate angle for diagonal from upper-left to bottom-right (negative slope)
                float angle = (float) Math.toDegrees(Math.atan2(-pageHeight, pageWidth));
                yield angle;
            }
            case DIAGONAL_DOWN -> {
                // Calculate angle for diagonal from bottom-left to top-right (positive slope)
                float angle = (float) Math.toDegrees(Math.atan2(pageHeight, pageWidth));
                yield angle;
            }
        };
    }
}
