package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.services.PageNumberService;
import com.xtopdf.xtopdf.services.PdfMergeService;
import com.xtopdf.xtopdf.services.WatermarkService;
import com.xtopdf.xtopdf.utils.PdfFileHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/pdf")
@AllArgsConstructor
@Slf4j
public class PdfOperationsController {
    private final PdfMergeService pdfMergeService;
    private final PageNumberService pageNumberService;
    private final WatermarkService watermarkService;

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(
            @RequestParam("pdf1") MultipartFile pdf1,
            @RequestParam("pdf2") MultipartFile pdf2,
            @RequestParam(value = "position", required = false, defaultValue = "back") String position) {
        
        // Validate position parameter
        if (!position.equalsIgnoreCase("front") && !position.equalsIgnoreCase("back")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        File tempPdf1 = null;
        
        try {
            // Create temporary file and write pdf1 to it
            tempPdf1 = PdfFileHelper.writeToTempFile(pdf1, "pdf1_");
            
            // Merge PDFs based on position
            if ("front".equalsIgnoreCase(position)) {
                pdfMergeService.mergePdfs(tempPdf1, pdf2, "front");
            } else {
                pdfMergeService.mergePdfs(tempPdf1, pdf2, "back");
            }
            
            // Read the merged PDF
            byte[] mergedPdfBytes = java.nio.file.Files.readAllBytes(tempPdf1.toPath());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"merged.pdf\"")
                    .body(mergedPdfBytes);
            
        } catch (Exception e) {
            log.error("Error merging PDFs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            // Clean up temporary file
            if (tempPdf1 != null && tempPdf1.exists()) {
                tempPdf1.delete();
            }
        }
    }

    @PostMapping("/add-page-numbers")
    public ResponseEntity<byte[]> addPageNumbers(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam(value = "position", required = false, defaultValue = "BOTTOM") String position,
            @RequestParam(value = "alignment", required = false, defaultValue = "CENTER") String alignment,
            @RequestParam(value = "style", required = false, defaultValue = "ARABIC") String style) {
        
        try {
            // Parse and validate parameters
            PageNumberConfig config = PageNumberConfig.builder()
                    .enabled(true)
                    .position(com.xtopdf.xtopdf.enums.PageNumberPosition.valueOf(position.toUpperCase()))
                    .alignment(com.xtopdf.xtopdf.enums.PageNumberAlignment.valueOf(alignment.toUpperCase()))
                    .style(com.xtopdf.xtopdf.enums.PageNumberStyle.valueOf(style.toUpperCase()))
                    .build();
            
            // Use helper to process PDF with page numbers
            return PdfFileHelper.processPdfFile(
                    pdfFile,
                    tempPdf -> pageNumberService.addPageNumbers(tempPdf, config),
                    "numbered.pdf"
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/add-watermark")
    public ResponseEntity<byte[]> addWatermark(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("watermarkText") String watermarkText,
            @RequestParam(value = "fontSize", required = false, defaultValue = "48") float fontSize,
            @RequestParam(value = "layer", required = false, defaultValue = "FOREGROUND") String layer,
            @RequestParam(value = "orientation", required = false, defaultValue = "DIAGONAL_UP") String orientation) {
        
        // Validate watermark text
        if (watermarkText == null || watermarkText.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        // Validate font size
        if (fontSize <= 0 || fontSize > 200) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        try {
            // Parse and validate parameters
            WatermarkConfig config = WatermarkConfig.builder()
                    .enabled(true)
                    .text(watermarkText)
                    .fontSize(fontSize)
                    .layer(com.xtopdf.xtopdf.enums.WatermarkLayer.valueOf(layer.toUpperCase()))
                    .orientation(com.xtopdf.xtopdf.enums.WatermarkOrientation.valueOf(orientation.toUpperCase()))
                    .build();
            
            // Use helper to process PDF with watermark
            return PdfFileHelper.processPdfFile(
                    pdfFile,
                    tempPdf -> watermarkService.addWatermark(tempPdf, config),
                    "watermarked.pdf"
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
