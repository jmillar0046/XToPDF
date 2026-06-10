package com.xtopdf.xtopdf.dto;

import java.time.Instant;

/**
 * Immutable record representing an async conversion job.
 * Tracks the lifecycle of a file conversion from submission to completion.
 *
 * @param id            Unique job identifier (UUID)
 * @param status        Current job status (PENDING, PROCESSING, COMPLETED, FAILED)
 * @param inputFileName Original input file name
 * @param outputFileName Generated output PDF file name
 * @param createdAt     Timestamp when the job was submitted
 * @param completedAt   Timestamp when the job completed (null if still in progress)
 * @param errorMessage  Error details if the job failed (null otherwise)
 * @param webhookUrl    Optional webhook URL to notify on completion
 */
public record ConversionJob(
        String id,
        JobStatus status,
        String inputFileName,
        String outputFileName,
        Instant createdAt,
        Instant completedAt,
        String errorMessage,
        String webhookUrl
) {
    public enum JobStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    /**
     * Creates a new pending job.
     */
    public static ConversionJob pending(String id, String inputFileName, String outputFileName, String webhookUrl) {
        return new ConversionJob(id, JobStatus.PENDING, inputFileName, outputFileName, Instant.now(), null, null, webhookUrl);
    }

    /**
     * Returns a copy of this job with status set to PROCESSING.
     */
    public ConversionJob withProcessing() {
        return new ConversionJob(id, JobStatus.PROCESSING, inputFileName, outputFileName, createdAt, null, null, webhookUrl);
    }

    /**
     * Returns a copy of this job with status set to COMPLETED.
     */
    public ConversionJob withCompleted() {
        return new ConversionJob(id, JobStatus.COMPLETED, inputFileName, outputFileName, createdAt, Instant.now(), null, webhookUrl);
    }

    /**
     * Returns a copy of this job with status set to FAILED and an error message.
     */
    public ConversionJob withFailed(String error) {
        return new ConversionJob(id, JobStatus.FAILED, inputFileName, outputFileName, createdAt, Instant.now(), error, webhookUrl);
    }
}
