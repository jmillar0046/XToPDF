package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.enums.PageNumberAlignment;
import com.xtopdf.xtopdf.enums.PageNumberPosition;
import com.xtopdf.xtopdf.enums.PageNumberStyle;
import com.xtopdf.xtopdf.exceptions.FileConversionException;
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
             @RequestParam(value = "pageNumberStyle", required = false, defaultValue = "ARABIC") String pageNumberStyle) {
         var baseDirectory = Paths.get("/safe/output/directory").normalize().toAbsolutePath();
         var sanitizedOutputPath = baseDirectory.resolve(outputFile).normalize().toAbsolutePath();
         if (!sanitizedOutputPath.startsWith(baseDirectory) || !sanitizedOutputPath.toString().endsWith(".pdf")) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid output file path");
         }

         // Validate position parameter
         if (existingPdf != null && !position.equalsIgnoreCase("front") && !position.equalsIgnoreCase("back")) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid position. Must be 'front' or 'back'");
         }

         // Parse and validate page numbering parameters
         PageNumberConfig pageNumberConfig;
         if (addPageNumbers) {
             try {
                 PageNumberPosition pnPosition = PageNumberPosition.valueOf(pageNumberPosition.toUpperCase());
                 PageNumberAlignment pnAlignment = PageNumberAlignment.valueOf(pageNumberAlignment.toUpperCase());
                 PageNumberStyle pnStyle = PageNumberStyle.valueOf(pageNumberStyle.toUpperCase());
                 
                 pageNumberConfig = PageNumberConfig.builder()
                         .enabled(true)
                         .position(pnPosition)
                         .alignment(pnAlignment)
                         .style(pnStyle)
                         .build();
             } catch (IllegalArgumentException e) {
                 return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                         .body("Invalid page numbering parameters. Position must be TOP or BOTTOM, " +
                               "Alignment must be LEFT, CENTER, or RIGHT, " +
                               "Style must be ARABIC, ROMAN_UPPER, ROMAN_LOWER, ALPHABETIC_UPPER, or ALPHABETIC_LOWER");
             }
         } else {
             pageNumberConfig = PageNumberConfig.disabled();
         }

         try {
            fileConversionService.convertFile(inputFile, sanitizedOutputPath.toString(), existingPdf, position, pageNumberConfig);
            return ResponseEntity.ok("File converted successfully");
         } catch (FileConversionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error with conversion");
         }
     }
}
