package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.ConversionRequest;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.services.FileConversionService;
import com.xtopdf.xtopdf.utils.ConversionConfigHelper;
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

            // Use helper methods to convert DTOs to configs with validation
            String position = ConversionConfigHelper.extractMergePosition(request.getMerge());
            PageNumberConfig pageNumberConfig = ConversionConfigHelper.toPageNumberConfig(request.getPageNumbers());
            WatermarkConfig watermarkConfig = ConversionConfigHelper.toWatermarkConfig(request.getWatermark());
            boolean executeMacros = request.getExecuteMacros() != null && request.getExecuteMacros();

            fileConversionService.convertFile(inputFile, sanitizedOutputPath.toString(), existingPdf, 
                    position, pageNumberConfig, watermarkConfig, executeMacros);
            
            return ResponseEntity.ok("File converted successfully");
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error converting file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error with conversion");
        }
    }
}
