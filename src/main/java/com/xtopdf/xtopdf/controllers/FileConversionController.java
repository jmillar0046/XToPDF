package com.xtopdf.xtopdf.controllers;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtopdf.xtopdf.services.FileConversionService;

import java.io.File;
import java.util.Objects;

@RestController
@RequestMapping("/convert")
@AllArgsConstructor
public class FileConversionController {
     private final FileConversionService fileConversionService;

     @PostMapping
     public ResponseEntity<String> convertFile (@RequestParam("inputFile") File inputFile, @RequestParam("outputFile") String outputFile) {
         if(Objects.isNull(inputFile) || Objects.isNull(outputFile)){
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
         }
         try {
            fileConversionService.convertFile(inputFile, outputFile);
            return ResponseEntity.ok("File converted successfully");
         } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error with conversion");
         }
     }
}
