package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.dto.ConversionJob;
import com.xtopdf.xtopdf.dto.ConversionJob.JobStatus;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for the async conversion API job tracking.
 *
 * **Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5**
 */
class AsyncConversionPropertyTest {

    private JobTrackingService jobTrackingService;

    @BeforeProperty
    void setup() {
        jobTrackingService = new JobTrackingService();
    }

    /**
     * Property 28: For any valid input file name, submitting a job returns a valid UUID job ID immediately.
     *
     * **Validates: Requirements 12.1**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 28: Async Job ID Return")
    void submittingJobReturnsValidUuid(
            @ForAll("fileNames") String inputFileName) {
        var job = jobTrackingService.submit(inputFileName, inputFileName + ".pdf", null);

        assertThat(job).isNotNull();
        assertThat(job.id()).isNotNull().isNotBlank();
        // Validate UUID format
        assertThat(job.id()).matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        assertThat(job.status()).isEqualTo(JobStatus.PENDING);
        assertThat(job.inputFileName()).isEqualTo(inputFileName);
    }

    /**
     * Property 29: For any submitted job, getStatus returns the current job status.
     *
     * **Validates: Requirements 12.2**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 29: Job Status Tracking")
    void jobStatusIsTrackable(
            @ForAll("fileNames") String inputFileName) {
        var job = jobTrackingService.submit(inputFileName, inputFileName + ".pdf", null);

        var status = jobTrackingService.getStatus(job.id());
        assertThat(status).isPresent();
        assertThat(status.get().status()).isEqualTo(JobStatus.PENDING);

        // Transition to processing
        jobTrackingService.markProcessing(job.id());
        status = jobTrackingService.getStatus(job.id());
        assertThat(status).isPresent();
        assertThat(status.get().status()).isEqualTo(JobStatus.PROCESSING);
    }

    /**
     * Property 30: Status transitions follow correct order: PENDING → PROCESSING → COMPLETED.
     *
     * **Validates: Requirements 12.3**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 30: Job Completion Status")
    void completedJobHasCorrectStatus(
            @ForAll("fileNames") String inputFileName) {
        var job = jobTrackingService.submit(inputFileName, inputFileName + ".pdf", null);
        byte[] pdfBytes = "fake-pdf-content".getBytes();

        jobTrackingService.markProcessing(job.id());
        jobTrackingService.markCompleted(job.id(), pdfBytes);

        var status = jobTrackingService.getStatus(job.id());
        assertThat(status).isPresent();
        assertThat(status.get().status()).isEqualTo(JobStatus.COMPLETED);
        assertThat(status.get().completedAt()).isNotNull();

        var result = jobTrackingService.getResult(job.id());
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(pdfBytes);
    }

    /**
     * Property 31: Failed jobs have FAILED status and include an error message.
     *
     * **Validates: Requirements 12.4**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 31: Job Failure Status")
    void failedJobHasErrorMessage(
            @ForAll("fileNames") String inputFileName) {
        var job = jobTrackingService.submit(inputFileName, inputFileName + ".pdf", null);

        jobTrackingService.markProcessing(job.id());
        jobTrackingService.markFailed(job.id(), "Conversion failed");

        var status = jobTrackingService.getStatus(job.id());
        assertThat(status).isPresent();
        assertThat(status.get().status()).isEqualTo(JobStatus.FAILED);
        assertThat(status.get().errorMessage()).isEqualTo("Conversion failed");
        assertThat(status.get().completedAt()).isNotNull();
    }

    /**
     * Property 32: Polling for a non-existent job returns empty.
     *
     * **Validates: Requirements 12.5**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 32: Job Status Polling")
    void pollingNonExistentJobReturnsEmpty(
            @ForAll("jobIds") String jobId) {
        var status = jobTrackingService.getStatus(jobId);
        assertThat(status).isEmpty();
    }

    @Provide
    Arbitrary<String> fileNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(name -> name + ".docx");
    }

    @Provide
    Arbitrary<String> jobIds() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(10)
                .ofMaxLength(36);
    }
}
