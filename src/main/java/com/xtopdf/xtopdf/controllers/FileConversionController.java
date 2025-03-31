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

import java.io.File;
import java.util.Objects;

@RestController
@RequestMapping("/api/convert")
@AllArgsConstructor
public class FileConversionController {
     private final FileConversionService fileConversionService;

     @PostMapping
     public ResponseEntity<String> convertFile (@RequestParam("inputFile") MultipartFile inputFile, @RequestParam("outputFile") String outputFile) {
         if(outputFile.isEmpty()){
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing parameter");
         }
         try {
            fileConversionService.convertFile(inputFile, outputFile);
            return ResponseEntity.ok("File converted successfully");
         } catch (FileConversionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error with conversion");
         }
     }
}
