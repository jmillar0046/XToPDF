package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import com.xtopdf.xtopdf.dto.MergeRequest;
import com.xtopdf.xtopdf.dto.PageNumberRequest;
import com.xtopdf.xtopdf.dto.WatermarkRequest;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.enums.WatermarkLayer;
import com.xtopdf.xtopdf.enums.WatermarkOrientation;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
import com.xtopdf.xtopdf.utils.ConversionConfigHelper;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtopdf.xtopdf.services.FileConversionService;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/convert")
@AllArgsConstructor
public class FileConversionController {
     private final FileConversionService fileConversionService;

     @PostMapping
     public ResponseEntity<String> convertFile(
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
            @RequestParam(value = "watermarkOrientation", required = false, defaultValue = "DIAGONAL_UP") String watermarkOrientation) {
         var baseDirectory = Paths.get("/safe/output/directory").normalize().toAbsolutePath();
         var sanitizedOutputPath = baseDirectory.resolve(outputFile).normalize().toAbsolutePath();
         if (!sanitizedOutputPath.startsWith(baseDirectory) || !sanitizedOutputPath.toString().endsWith(".pdf")) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid output file path");
         }

         try {
             // Convert individual parameters to DTO objects for cleaner processing
             MergeRequest mergeRequest = existingPdf != null ? 
                     ConversionConfigHelper.createMergeRequest(position) : null;
             
             PageNumberRequest pageNumberRequest = null;
             if (addPageNumbers) {
                 try {
                     pageNumberRequest = ConversionConfigHelper.createPageNumberRequest(
                             PageNumberPosition.valueOf(pageNumberPosition.toUpperCase()),
                             PageNumberAlignment.valueOf(pageNumberAlignment.toUpperCase()),
                             PageNumberStyle.valueOf(pageNumberStyle.toUpperCase())
                     );
                 } catch (IllegalArgumentException e) {
                     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("Invalid page numbering parameters. Position must be TOP or BOTTOM, " +
                                   "Alignment must be LEFT, CENTER, or RIGHT, " +
                                   "Style must be ARABIC, ROMAN_UPPER, ROMAN_LOWER, ALPHABETIC_UPPER, or ALPHABETIC_LOWER");
                 }
             }
             
             WatermarkRequest watermarkRequest = null;
             if (addWatermark) {
                 try {
                     watermarkRequest = ConversionConfigHelper.createWatermarkRequest(
                             watermarkText,
                             watermarkFontSize,
                             WatermarkLayer.valueOf(watermarkLayer.toUpperCase()),
                             WatermarkOrientation.valueOf(watermarkOrientation.toUpperCase())
                     );
                 } catch (IllegalArgumentException e) {
                     return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("Invalid watermark parameters. Layer must be FOREGROUND or BACKGROUND, " +
                                   "Orientation must be HORIZONTAL, VERTICAL, DIAGONAL_UP, or DIAGONAL_DOWN");
                 }
             }
             
             // Validate and convert to config objects
             String validatedPosition = ConversionConfigHelper.extractMergePosition(mergeRequest);
             PageNumberConfig pageNumberConfig = ConversionConfigHelper.toPageNumberConfig(pageNumberRequest);
             WatermarkConfig watermarkConfig = ConversionConfigHelper.toWatermarkConfig(watermarkRequest);
             
             fileConversionService.convertFile(inputFile, sanitizedOutputPath.toString(), existingPdf, 
                     validatedPosition, pageNumberConfig, watermarkConfig, executeMacros);
             return ResponseEntity.ok("File converted successfully");
             
         } catch (IllegalArgumentException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
         } catch (FileConversionException e) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error with conversion");
         }
     }
}
