package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.BatchConversionResult;
import com.xtopdf.xtopdf.services.BatchConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for batch file conversion operations.
 * Accepts multiple files and processes them in parallel.
 */
@RestController
@RequestMapping("/api/convert/batch")
@AllArgsConstructor
@Slf4j
@Tag(name = "Batch Conversion", description = "Endpoints for batch file conversion to PDF")
public class BatchConversionController {

    private final BatchConversionService batchConversionService;

    /**
     * Converts multiple files to PDF in parallel.
     *
     * @param files the list of files to convert
     * @return a structured response with per-file results
     */
    @PostMapping
    @Operation(summary = "Batch convert files to PDF", description = "Accepts multiple files and converts them to PDF in parallel using virtual threads")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Batch processing completed (individual results may vary)"),
            @ApiResponse(responseCode = "400", description = "Batch size exceeds maximum allowed"),
            @ApiResponse(responseCode = "413", description = "Request size exceeds maximum limit"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<BatchConversionResult> convertBatch(
            @Parameter(description = "List of files to convert to PDF")
            @RequestParam("files") List<MultipartFile> files) {
        log.info("Batch conversion request received with {} files", files.size());
        var result = batchConversionService.convertBatch(files);
        return ResponseEntity.ok(result);
    }
}
