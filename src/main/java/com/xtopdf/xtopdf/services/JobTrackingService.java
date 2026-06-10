package com.xtopdf.xtopdf.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xtopdf.xtopdf.dto.ConversionJob;
import com.xtopdf.xtopdf.dto.ConversionJob.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for tracking async conversion jobs in-memory.
 * Uses Caffeine caches with eviction to prevent memory leaks.
 */
@Service
@Slf4j
public class JobTrackingService {

    private final Cache<String, ConversionJob> jobs = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofHours(2))
            .build();

    private final Cache<String, byte[]> results = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    /**
     * Submits a new conversion job and returns the job with a generated UUID.
     *
     * @param inputFileName  the original input file name
     * @param outputFileName the generated output file name
     * @param webhookUrl     optional webhook URL to notify on completion
     * @return the created ConversionJob in PENDING status
     */
    public ConversionJob submit(String inputFileName, String outputFileName, String webhookUrl) {
        String jobId = UUID.randomUUID().toString();
        var job = ConversionJob.pending(jobId, inputFileName, outputFileName, webhookUrl);
        jobs.put(jobId, job);
        log.info("Job submitted: id={}, inputFile={}", jobId, inputFileName);
        return job;
    }

    /**
     * Gets the current status of a job.
     *
     * @param jobId the job ID
     * @return the job if found, empty otherwise
     */
    public Optional<ConversionJob> getStatus(String jobId) {
        return Optional.ofNullable(jobs.getIfPresent(jobId));
    }

    /**
     * Gets the result bytes of a completed job.
     *
     * @param jobId the job ID
     * @return the PDF bytes if the job is completed, empty otherwise
     */
    public Optional<byte[]> getResult(String jobId) {
        return Optional.ofNullable(results.getIfPresent(jobId));
    }

    /**
     * Updates the job status to PROCESSING.
     *
     * @param jobId the job ID
     */
    public void markProcessing(String jobId) {
        var existing = jobs.getIfPresent(jobId);
        if (existing != null) {
            jobs.put(jobId, existing.withProcessing());
        }
        log.debug("Job processing: id={}", jobId);
    }

    /**
     * Updates the job status to COMPLETED and stores the result.
     *
     * @param jobId    the job ID
     * @param pdfBytes the converted PDF bytes
     */
    public void markCompleted(String jobId, byte[] pdfBytes) {
        var existing = jobs.getIfPresent(jobId);
        if (existing != null) {
            jobs.put(jobId, existing.withCompleted());
        }
        results.put(jobId, pdfBytes);
        log.info("Job completed: id={}", jobId);
    }

    /**
     * Updates the job status to FAILED with an error message.
     *
     * @param jobId        the job ID
     * @param errorMessage description of what went wrong
     */
    public void markFailed(String jobId, String errorMessage) {
        var existing = jobs.getIfPresent(jobId);
        if (existing != null) {
            jobs.put(jobId, existing.withFailed(errorMessage));
        }
        log.error("Job failed: id={}, error={}", jobId, errorMessage);
    }
}
