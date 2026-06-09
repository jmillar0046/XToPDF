package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.dto.ConversionJob;
import com.xtopdf.xtopdf.dto.ConversionJob.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for tracking async conversion jobs in-memory.
 * Uses ConcurrentHashMap for thread-safe job storage.
 */
@Service
@Slf4j
public class JobTrackingService {

    private final ConcurrentMap<String, ConversionJob> jobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, byte[]> results = new ConcurrentHashMap<>();

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
        return Optional.ofNullable(jobs.get(jobId));
    }

    /**
     * Gets the result bytes of a completed job.
     *
     * @param jobId the job ID
     * @return the PDF bytes if the job is completed, empty otherwise
     */
    public Optional<byte[]> getResult(String jobId) {
        return Optional.ofNullable(results.get(jobId));
    }

    /**
     * Updates the job status to PROCESSING.
     *
     * @param jobId the job ID
     */
    public void markProcessing(String jobId) {
        jobs.computeIfPresent(jobId, (id, job) -> job.withProcessing());
        log.debug("Job processing: id={}", jobId);
    }

    /**
     * Updates the job status to COMPLETED and stores the result.
     *
     * @param jobId    the job ID
     * @param pdfBytes the converted PDF bytes
     */
    public void markCompleted(String jobId, byte[] pdfBytes) {
        jobs.computeIfPresent(jobId, (id, job) -> job.withCompleted());
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
        jobs.computeIfPresent(jobId, (id, job) -> job.withFailed(errorMessage));
        log.error("Job failed: id={}, error={}", jobId, errorMessage);
    }
}
