package com.xtopdf.xtopdf.controllers;

import com.xtopdf.xtopdf.dto.ConversionJob;
import com.xtopdf.xtopdf.dto.ConversionJob.JobStatus;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.services.FileConversionService;
import com.xtopdf.xtopdf.services.JobTrackingService;
import com.xtopdf.xtopdf.services.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;

/**
 * Controller for async file conversion operations.
 * Submits jobs that run in background virtual threads, returning a job ID immediately.
 */
@RestController
@RequestMapping({"/api/convert/async", "/v1/api/convert/async"})
@Slf4j
@Tag(name = "Async Conversion", description = "Endpoints for asynchronous file conversion with job tracking")
public class AsyncConversionController {

    private final FileConversionService fileConversionService;
    private final JobTrackingService jobTrackingService;
    private final WebhookService webhookService;
    private final String baseOutputDirectory;
    private final ExecutorService executor;

    public AsyncConversionController(
            FileConversionService fileConversionService,
            JobTrackingService jobTrackingService,
            WebhookService webhookService,
            @Value("${xtopdf.output.directory:/safe/output/directory}") String baseOutputDirectory) {
        this.fileConversionService = fileConversionService;
        this.jobTrackingService = jobTrackingService;
        this.webhookService = webhookService;
        this.baseOutputDirectory = baseOutputDirectory;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Submits a file conversion job for async processing.
     * Returns a job ID immediately without waiting for conversion.
     */
    @PostMapping
    @Operation(summary = "Submit async conversion", description = "Submits a file for async conversion and returns a job ID immediately")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Job submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "413", description = "File size exceeds maximum limit"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    public ResponseEntity<Map<String, Object>> submitJob(
            @RequestParam("inputFile") MultipartFile inputFile,
            @RequestParam(value = "webhookUrl", required = false) String webhookUrl) {

        String inputFileName = inputFile.getOriginalFilename();
        if (inputFileName == null || inputFileName.isBlank()) {
            throw new IllegalArgumentException("Input file name is required");
        }

        String outputFileName = generateOutputFileName(inputFileName);
        var job = jobTrackingService.submit(inputFileName, outputFileName, webhookUrl);

        // Store file bytes for async processing
        byte[] fileBytes;
        try {
            fileBytes = inputFile.getBytes();
        } catch (Exception e) {
            jobTrackingService.markFailed(job.id(), "Failed to read input file");
            throw new IllegalArgumentException("Failed to read input file");
        }

        // Submit to virtual thread for background processing
        executor.submit(() -> processJob(job.id(), inputFileName, outputFileName, fileBytes, webhookUrl));

        var response = Map.<String, Object>of(
                "jobId", job.id(),
                "status", job.status().name(),
                "message", "Job submitted successfully"
        );
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Returns the current status of a job.
     */
    @GetMapping("/{jobId}")
    @Operation(summary = "Get job status", description = "Returns the current status of an async conversion job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job status returned"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        return jobTrackingService.getStatus(jobId)
                .map(job -> {
                    var response = new java.util.HashMap<String, Object>();
                    response.put("jobId", job.id());
                    response.put("status", job.status().name());
                    response.put("inputFileName", job.inputFileName());
                    response.put("outputFileName", job.outputFileName());
                    response.put("createdAt", job.createdAt().toString());
                    if (job.completedAt() != null) {
                        response.put("completedAt", job.completedAt().toString());
                    }
                    if (job.errorMessage() != null) {
                        response.put("errorMessage", job.errorMessage());
                    }
                    return ResponseEntity.ok(Map.copyOf(response));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Returns the PDF result of a completed job.
     */
    @GetMapping("/{jobId}/result")
    @Operation(summary = "Get job result", description = "Returns the converted PDF file for a completed job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF file returned"),
            @ApiResponse(responseCode = "404", description = "Job not found or not completed"),
            @ApiResponse(responseCode = "409", description = "Job not yet completed")
    })
    public ResponseEntity<byte[]> getJobResult(@PathVariable String jobId) {
        var jobOpt = jobTrackingService.getStatus(jobId);
        if (jobOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var job = jobOpt.get();
        if (job.status() != JobStatus.COMPLETED) {
            return ResponseEntity.status(409).build();
        }

        return jobTrackingService.getResult(jobId)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + job.outputFileName() + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(bytes))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private void processJob(String jobId, String inputFileName, String outputFileName, byte[] fileBytes, String webhookUrl) {
        try {
            jobTrackingService.markProcessing(jobId);

            var baseDirectory = Paths.get(baseOutputDirectory).normalize().toAbsolutePath();
            var outputPath = baseDirectory.resolve(outputFileName).normalize().toAbsolutePath();

            if (!outputPath.startsWith(baseDirectory) || !outputPath.toString().endsWith(".pdf")) {
                jobTrackingService.markFailed(jobId, "Invalid output file path");
                notifyWebhook(jobId, webhookUrl);
                return;
            }

            // Create a MultipartFile from stored bytes for processing
            var tempInputFile = new InMemoryMultipartFile(inputFileName, fileBytes);

            ConversionParameters params = ConversionParameters.of(tempInputFile, outputPath.toString());
            fileConversionService.convertFile(params);

            // Read the generated PDF
            byte[] pdfBytes = Files.readAllBytes(outputPath);
            jobTrackingService.markCompleted(jobId, pdfBytes);

            // Clean up the temp output file
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            log.error("Async conversion failed for job {}: {}", jobId, e.getMessage(), e);
            jobTrackingService.markFailed(jobId, "Conversion failed");
        }

        notifyWebhook(jobId, webhookUrl);
    }

    private void notifyWebhook(String jobId, String webhookUrl) {
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            jobTrackingService.getStatus(jobId).ifPresent(job ->
                    webhookService.notifyCompletion(job, webhookUrl));
        }
    }

    private String generateOutputFileName(String inputFileName) {
        int dotIndex = inputFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? inputFileName.substring(0, dotIndex) : inputFileName;
        return baseName + "-" + System.currentTimeMillis() + ".pdf";
    }

    /**
     * Simple in-memory MultipartFile implementation for async processing.
     * Stores file bytes captured at submission time.
     */
    private record InMemoryMultipartFile(String originalFilename, byte[] bytes) implements MultipartFile {
        @Override
        public String getName() {
            return "inputFile";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return bytes == null || bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes != null ? bytes.length : 0;
        }

        @Override
        public byte[] getBytes() {
            return bytes != null ? bytes : new byte[0];
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(getBytes());
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            Files.write(dest.toPath(), getBytes());
        }
    }
}
