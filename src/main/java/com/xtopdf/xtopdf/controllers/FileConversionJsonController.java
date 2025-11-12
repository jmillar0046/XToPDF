package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.ConversionRequest;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.FileConversionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@RestController
@RequestMapping("/api/convert-json")
@AllArgsConstructor
@Slf4j
public class FileConversionJsonController {
    private final FileConversionService fileConversionService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> convertFile(
            @RequestPart("inputFile") MultipartFile inputFile,
            @RequestPart(value = "existingPdf", required = false) MultipartFile existingPdf,
            @RequestPart("request") String requestJson) {
        
        try {
            // Parse JSON request
            ConversionRequest request = objectMapper.readValue(requestJson, ConversionRequest.class);
            
            var baseDirectory = Paths.get("/safe/output/directory").normalize().toAbsolutePath();
            var sanitizedOutputPath = baseDirectory.resolve(request.getOutputFile()).normalize().toAbsolutePath();
            if (!sanitizedOutputPath.startsWith(baseDirectory) || !sanitizedOutputPath.toString().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid output file path");
            }

            // Extract merge configuration
            String position = "back";
            if (request.getMerge() != null && request.getMerge().getPosition() != null) {
                position = request.getMerge().getPosition();
                if (!position.equalsIgnoreCase("front") && !position.equalsIgnoreCase("back")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid position. Must be 'front' or 'back'");
                }
            }

            // Extract page numbering configuration
            PageNumberConfig pageNumberConfig;
            if (request.getPageNumbers() != null) {
                pageNumberConfig = PageNumberConfig.builder()
                        .enabled(true)
                        .position(request.getPageNumbers().getPosition() != null ? 
                                request.getPageNumbers().getPosition() : PageNumberPosition.BOTTOM)
                        .alignment(request.getPageNumbers().getAlignment() != null ? 
                                request.getPageNumbers().getAlignment() : PageNumberAlignment.CENTER)
                        .style(request.getPageNumbers().getStyle() != null ? 
                                request.getPageNumbers().getStyle() : PageNumberStyle.ARABIC)
                        .build();
            } else {
                pageNumberConfig = PageNumberConfig.disabled();
            }

            // Extract watermark configuration
            WatermarkConfig watermarkConfig;
            if (request.getWatermark() != null) {
                String text = request.getWatermark().getText();
                if (text == null || text.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Watermark text must be provided when watermark configuration is specified");
                }
                
                float fontSize = request.getWatermark().getFontSize() != null ? 
                        request.getWatermark().getFontSize() : 48f;
                if (fontSize <= 0 || fontSize > 200) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Watermark font size must be greater than 0 and up to 200");
                }
                
                watermarkConfig = WatermarkConfig.builder()
                        .enabled(true)
                        .text(text)
                        .fontSize(fontSize)
                        .layer(request.getWatermark().getLayer() != null ? 
                                request.getWatermark().getLayer() : WatermarkLayer.FOREGROUND)
                        .orientation(request.getWatermark().getOrientation() != null ? 
                                request.getWatermark().getOrientation() : WatermarkOrientation.DIAGONAL_UP)
                        .build();
            } else {
                watermarkConfig = WatermarkConfig.disabled();
            }

            boolean executeMacros = request.getExecuteMacros() != null && request.getExecuteMacros();

            fileConversionService.convertFile(inputFile, sanitizedOutputPath.toString(), existingPdf, 
                    position, pageNumberConfig, watermarkConfig, executeMacros);
            
            return ResponseEntity.ok("File converted successfully");
            
        } catch (Exception e) {
            log.error("Error converting file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error with conversion: " + e.getMessage());
        }
    }
}
