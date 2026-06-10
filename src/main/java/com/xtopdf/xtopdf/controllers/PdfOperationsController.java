package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.services.operations.PageNumberService;
import com.xtopdf.xtopdf.services.operations.PdfMergeService;
import com.xtopdf.xtopdf.services.operations.WatermarkService;
import com.xtopdf.xtopdf.utils.PdfFileHelper;
import com.xtopdf.xtopdf.validation.PdfContentValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping({"/api/pdf", "/v1/api/pdf"})
@AllArgsConstructor
@Slf4j
@Tag(name = "PDF Operations", description = "Endpoints for PDF manipulation operations (merge, page numbers, watermarks)")
public class PdfOperationsController {
    private final PdfMergeService pdfMergeService;
    private final PageNumberService pageNumberService;
    private final WatermarkService watermarkService;

    @PostMapping("/merge")
    @Operation(summary = "Merge two PDFs", description = "Merges two PDF files together at the specified position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDFs merged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF file or parameters")
    })
    public ResponseEntity<byte[]> mergePdfs(
            @RequestParam("pdf1") MultipartFile pdf1,
            @RequestParam("pdf2") MultipartFile pdf2,
            @RequestParam(value = "position", required = false, defaultValue = "back") String position) {
        
        // Validate PDF content
        try {
            if (!PdfContentValidator.isPdf(pdf1)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"File 'pdf1' is not a valid PDF\"}".getBytes());
            }
            if (!PdfContentValidator.isPdf(pdf2)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"File 'pdf2' is not a valid PDF\"}".getBytes());
            }
        } catch (IOException e) {
            log.error("Error validating PDF content: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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
    @Operation(summary = "Add page numbers to PDF", description = "Adds page numbers to an existing PDF with configurable position, alignment, and style")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page numbers added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF file or parameters")
    })
    public ResponseEntity<byte[]> addPageNumbers(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam(value = "position", required = false, defaultValue = "BOTTOM") String position,
            @RequestParam(value = "alignment", required = false, defaultValue = "CENTER") String alignment,
            @RequestParam(value = "style", required = false, defaultValue = "ARABIC") String style) {
        
        // Validate PDF content
        try {
            if (!PdfContentValidator.isPdf(pdfFile)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"File is not a valid PDF\"}".getBytes());
            }
        } catch (IOException e) {
            log.error("Error validating PDF content: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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
    @Operation(summary = "Add watermark to PDF", description = "Adds a text watermark to an existing PDF with configurable font size, layer, and orientation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Watermark added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF file or parameters")
    })
    public ResponseEntity<byte[]> addWatermark(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("watermarkText") String watermarkText,
            @RequestParam(value = "fontSize", required = false, defaultValue = "48") float fontSize,
            @RequestParam(value = "layer", required = false, defaultValue = "FOREGROUND") String layer,
            @RequestParam(value = "orientation", required = false, defaultValue = "DIAGONAL_UP") String orientation) {
        
        // Validate PDF content
        try {
            if (!PdfContentValidator.isPdf(pdfFile)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"File is not a valid PDF\"}".getBytes());
            }
        } catch (IOException e) {
            log.error("Error validating PDF content: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

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
