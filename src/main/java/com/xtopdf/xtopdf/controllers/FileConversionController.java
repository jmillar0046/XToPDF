package com.xtopdf.xtopdf.controllers;

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
             @RequestParam(value = "position", required = false, defaultValue = "back") String position) {
         var baseDirectory = Paths.get("/safe/output/directory").normalize().toAbsolutePath();
         var sanitizedOutputPath = baseDirectory.resolve(outputFile).normalize().toAbsolutePath();
         if (!sanitizedOutputPath.startsWith(baseDirectory) || !sanitizedOutputPath.toString().endsWith(".pdf")) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid output file path");
         }

         // Validate position parameter
         if (existingPdf != null && !position.equalsIgnoreCase("front") && !position.equalsIgnoreCase("back")) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid position. Must be 'front' or 'back'");
         }

         try {
            fileConversionService.convertFile(inputFile, sanitizedOutputPath.toString(), existingPdf, position);
            return ResponseEntity.ok("File converted successfully");
         } catch (FileConversionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error with conversion");
         }
     }
}
