package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.config.BatchConfig;
import com.xtopdf.xtopdf.dto.BatchConversionResult;
import com.xtopdf.xtopdf.dto.BatchConversionResult.FileResult;
import com.xtopdf.xtopdf.dto.ConversionParameters;
import com.xtopdf.xtopdf.config.PageNumberConfig;
import com.xtopdf.xtopdf.config.WatermarkConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for processing batch file conversions in parallel using virtual threads.
 */
@Service
@Slf4j
public class BatchConversionService {

    private final FileConversionService fileConversionService;
    private final BatchConfig batchConfig;
    private final String baseOutputDirectory;

    public BatchConversionService(
            FileConversionService fileConversionService,
            BatchConfig batchConfig,
            @Value("${xtopdf.output.directory:/safe/output/directory}") String baseOutputDirectory) {
        this.fileConversionService = fileConversionService;
        this.batchConfig = batchConfig;
        this.baseOutputDirectory = baseOutputDirectory;
    }

    /**
     * Processes a batch of files in parallel using virtual threads.
     *
     * @param files the list of files to convert
     * @return a BatchConversionResult with per-file results
     */
    public BatchConversionResult convertBatch(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return BatchConversionResult.of(List.of());
        }

        if (files.size() > batchConfig.getMaxBatchSize()) {
            throw new IllegalArgumentException(
                    "Batch size exceeds maximum allowed: " + batchConfig.getMaxBatchSize());
        }

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        try {
            List<CompletableFuture<FileResult>> futures = files.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> convertSingleFile(file), executor))
                    .toList();

            List<FileResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            return BatchConversionResult.of(results);
        } finally {
            executor.shutdown();
        }
    }

    private FileResult convertSingleFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            return FileResult.failure("unknown", "File name is required");
        }

        try {
            var baseDirectory = Paths.get(baseOutputDirectory).normalize().toAbsolutePath();
            String outputFileName = generateOutputFileName(fileName);
            var outputPath = baseDirectory.resolve(outputFileName).normalize().toAbsolutePath();

            if (!outputPath.startsWith(baseDirectory) || !outputPath.toString().endsWith(".pdf")) {
                return FileResult.failure(fileName, "Invalid output file path");
            }

            ConversionParameters params = new ConversionParameters(
                    file,
                    outputPath.toString(),
                    null,
                    null,
                    PageNumberConfig.disabled(),
                    WatermarkConfig.disabled(),
                    false
            );

            fileConversionService.convertFile(params);
            return FileResult.success(fileName);
        } catch (Exception e) {
            log.error("Batch conversion failed for file {}: {}", fileName, e.getMessage(), e);
            return FileResult.failure(fileName, "Conversion failed");
        }
    }

    private String generateOutputFileName(String inputFileName) {
        int dotIndex = inputFileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? inputFileName.substring(0, dotIndex) : inputFileName;
        return baseName + "-" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
    }
}
