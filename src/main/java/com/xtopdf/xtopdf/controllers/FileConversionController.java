package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.dto.ConversionRequest;
import com.xtopdf.xtopdf.dto.MergeRequest;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.SuccessResponse;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.utils.ConversionConfigHelper;
import tools.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.xtopdf.xtopdf.services.FileConversionService;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;

@RestController
@RequestMapping({"/api/convert", "/v1/api/convert"})
@Slf4j
@Tag(name = "File Conversion", description = "Endpoints for converting files to PDF format")
public class FileConversionController {
     private final FileConversionService fileConversionService;
     private final ObjectMapper objectMapper;

     @Value("${xtopdf.output.directory:/safe/output/directory}")
     private String baseOutputDirectory;

     public FileConversionController(
             FileConversionService fileConversionService,
             ObjectMapper objectMapper) {
         this.fileConversionService = fileConversionService;
         this.objectMapper = objectMapper;
     }

     @PostMapping
     @Operation(summary = "Convert file to PDF", description = "Converts a single file to PDF format with optional page numbers, watermarks, and merging")
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "File converted successfully"),
             @ApiResponse(responseCode = "400", description = "Invalid request parameters or conversion error"),
             @ApiResponse(responseCode = "413", description = "File size exceeds maximum limit"),
             @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
             @ApiResponse(responseCode = "504", description = "Conversion timed out")
     })
     public ResponseEntity<SuccessResponse> convertFile(
             @RequestParam("inputFile") MultipartFile inputFile, 
             @RequestParam("outputFile") String outputFile,
             @RequestParam(value = "existingPdf", required = false) MultipartFile existingPdf,
             @RequestParam(value = "position", required = false, defaultValue = "back") String position,
             @RequestParam(value = "addPageNumbers", required = false, defaultValue = "false") boolean addPageNumbers,
             @RequestParam(value = "pageNumberPosition", required = false, defaultValue = "BOTTOM") String pageNumberPosition,
             @RequestParam(value = "pageNumberAlignment", required = false, defaultValue = "CENTER") String pageNumberAlignment,
             @RequestParam(value = "pageNumberStyle", required = false, defaultValue = "ARABIC") String pageNumberStyle,
             @RequestParam(value = "executeMacros", required = false, defaultValue = "false") boolean executeMacros,
            @RequestParam(value = "addWatermark", required = false, defaultValue = "false") boolean addWatermark,
            @RequestParam(value = "watermarkText", required = false) String watermarkText,
            @RequestParam(value = "watermarkFontSize", required = false, defaultValue = "48") float watermarkFontSize,
            @RequestParam(value = "watermarkLayer", required = false, defaultValue = "FOREGROUND") String watermarkLayer,
            @RequestParam(value = "watermarkOrientation", required = false, defaultValue = "DIAGONAL_UP") String watermarkOrientation)
             throws FileConversionException {
         var baseDirectory = Paths.get(baseOutputDirectory).normalize().toAbsolutePath();
         var sanitizedOutputPath = baseDirectory.resolve(outputFile).normalize().toAbsolutePath();
         if (!sanitizedOutputPath.startsWith(baseDirectory) || !sanitizedOutputPath.toString().endsWith(".pdf")) {
             throw new IllegalArgumentException("Invalid output file path");
         }

         // Convert individual parameters to DTO objects for cleaner processing
         MergeRequest mergeRequest = existingPdf != null ? 
                 ConversionConfigHelper.createMergeRequest(position) : null;
         
         PageNumberRequest pageNumberRequest = null;
         if (addPageNumbers) {
             pageNumberRequest = ConversionConfigHelper.createPageNumberRequest(
                     PageNumberPosition.valueOf(pageNumberPosition.toUpperCase()),
                     PageNumberAlignment.valueOf(pageNumberAlignment.toUpperCase()),
                     PageNumberStyle.valueOf(pageNumberStyle.toUpperCase())
             );
         }
         
         WatermarkRequest watermarkRequest = null;
         if (addWatermark) {
             watermarkRequest = ConversionConfigHelper.createWatermarkRequest(
                     watermarkText,
                     watermarkFontSize,
                     WatermarkLayer.valueOf(watermarkLayer.toUpperCase()),
                     WatermarkOrientation.valueOf(watermarkOrientation.toUpperCase())
             );
         }
         
         // Validate and convert to config objects
         String validatedPosition = ConversionConfigHelper.extractMergePosition(mergeRequest);
         PageNumberConfig pageNumberConfig = ConversionConfigHelper.toPageNumberConfig(pageNumberRequest);
         WatermarkConfig watermarkConfig = ConversionConfigHelper.toWatermarkConfig(watermarkRequest);
         
         ConversionParameters params = new ConversionParameters(
                 inputFile, sanitizedOutputPath.toString(), existingPdf,
                 validatedPosition, pageNumberConfig, watermarkConfig, executeMacros);
         fileConversionService.convertFile(params);
         return ResponseEntity.ok(SuccessResponse.ok("File converted successfully"));
     }

    /**
     * JSON-based conversion endpoint that accepts structured request object.
     * Supports the same features as the main endpoint but with a cleaner JSON structure.
     */
    @PostMapping(value = "/json", consumes = "multipart/form-data")
    @Operation(summary = "Convert file to PDF (JSON config)", description = "Converts a file to PDF using a JSON configuration object for structured request parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File converted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or conversion error"),
            @ApiResponse(responseCode = "413", description = "File size exceeds maximum limit"),
            @ApiResponse(responseCode = "504", description = "Conversion timed out")
    })
    public ResponseEntity<SuccessResponse> convertFileWithJson(
            @RequestPart("inputFile") MultipartFile inputFile,
            @RequestPart(value = "existingPdf", required = false) MultipartFile existingPdf,
            @Valid @RequestPart("request") String requestJson) throws Exception {
        
        // Parse JSON request
        ConversionRequest request = objectMapper.readValue(requestJson, ConversionRequest.class);
        
        var baseDirectory = Paths.get(baseOutputDirectory).normalize().toAbsolutePath();
        var sanitizedOutputPath = baseDirectory.resolve(request.getOutputFile()).normalize().toAbsolutePath();
        if (!sanitizedOutputPath.startsWith(baseDirectory) || !sanitizedOutputPath.toString().endsWith(".pdf")) {
            throw new IllegalArgumentException("Invalid output file path");
        }

        // Use helper methods to convert DTOs to configs with validation
        String position = ConversionConfigHelper.extractMergePosition(request.getMerge());
        PageNumberConfig pageNumberConfig = ConversionConfigHelper.toPageNumberConfig(request.getPageNumbers());
        WatermarkConfig watermarkConfig = ConversionConfigHelper.toWatermarkConfig(request.getWatermark());
        boolean executeMacros = request.getExecuteMacros() != null && request.getExecuteMacros();

        ConversionParameters params = new ConversionParameters(
                inputFile, sanitizedOutputPath.toString(), existingPdf,
                position, pageNumberConfig, watermarkConfig, executeMacros);
        fileConversionService.convertFile(params);
        
        return ResponseEntity.ok(SuccessResponse.ok("File converted successfully"));
    }
}
