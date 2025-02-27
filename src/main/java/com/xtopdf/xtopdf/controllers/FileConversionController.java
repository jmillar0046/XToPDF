package com.xtopdf.xtopdf.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtopdf.xtopdf.services.FileConversionService;

@RestController
@RequestMapping("/convert")
public class FileConversionController {
     @Autowired
     private FileConversionService fileConversionService;

     @PostMapping
     public ResponseEntity<String> convertFile (@RequestParam("inputFile") String inputFile, @RequestParam("outputFile") String outputFile) {
        try {
            fileConversionService.convertFile(inputFile, outputFile);
            return ResponseEntity.ok("File converted successfully")
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error with conversion");
        }
     }
}
