package com.xtopdf.xtopdf.services;

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
        if (pdfFile.delete()) {
            if (!tempFile.renameTo(pdfFile)) {
                throw new IOException("Failed to replace original PDF with watermarked version");
            }
        } else {
            tempFile.delete();
            throw new IOException("Failed to delete original PDF");
        }
    }
    
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
