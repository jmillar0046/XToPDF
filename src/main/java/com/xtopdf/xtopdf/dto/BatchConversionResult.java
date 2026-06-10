package com.xtopdf.xtopdf.dto;

import java.util.List;

/**
 * Response DTO for batch conversion operations.
 * Contains overall status and per-file results.
 */
public record BatchConversionResult(
        String status,
        int totalFiles,
        int successCount,
        int failureCount,
        List<FileResult> results
) {

    /**
     * Result for a single file in the batch.
     */
    public record FileResult(
            String fileName,
            String status,
            String message
    ) {
        public static FileResult success(String fileName) {
            return new FileResult(fileName, "success", "File converted successfully");
        }

        public static FileResult failure(String fileName, String message) {
            return new FileResult(fileName, "failed", message);
        }
    }

    public static BatchConversionResult of(List<FileResult> results) {
        int successCount = (int) results.stream().filter(r -> "success".equals(r.status())).count();
        int failureCount = results.size() - successCount;
        String status = failureCount == 0 ? "success" : (successCount == 0 ? "failed" : "partial");
        return new BatchConversionResult(status, results.size(), successCount, failureCount, results);
    }
}
