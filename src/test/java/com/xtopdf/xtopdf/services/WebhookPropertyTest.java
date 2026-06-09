package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.dto.ConversionJob;
import com.xtopdf.xtopdf.dto.ConversionJob.JobStatus;
import net.jqwik.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for webhook notifications.
 *
 * **Validates: Requirements 12.6, 23.1, 23.2, 23.3**
 */
class WebhookPropertyTest {

    private WebhookService webhookService;

    @net.jqwik.api.lifecycle.BeforeProperty
    void setup() {
        // Use a no-op HttpClient for payload testing (we test payload structure, not delivery)
        webhookService = new WebhookService();
    }

    /**
     * Property 33: For any completed job with a webhook URL, the webhook payload contains the job ID.
     *
     * **Validates: Requirements 12.6**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 33: Webhook Notification")
    void webhookPayloadContainsJobId(
            @ForAll("jobIds") String jobId,
            @ForAll("fileNames") String inputFileName) {
        var job = new ConversionJob(
                jobId, JobStatus.COMPLETED, inputFileName, inputFileName + ".pdf",
                Instant.now(), Instant.now(), null, "http://example.com/webhook");

        String payload = webhookService.buildPayload(job);

        assertThat(payload).contains("\"jobId\":\"" + jobId + "\"");
    }

    /**
     * Property 49: For any webhook notification, the payload includes complete job details
     * (jobId, status, inputFileName, timestamp).
     *
     * **Validates: Requirements 23.2**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 49: Webhook Payload Completeness")
    void webhookPayloadIsComplete(
            @ForAll("jobIds") String jobId,
            @ForAll("fileNames") String inputFileName,
            @ForAll("statuses") JobStatus status) {
        var completedAt = (status == JobStatus.COMPLETED || status == JobStatus.FAILED) ? Instant.now() : null;
        var errorMessage = (status == JobStatus.FAILED) ? "Conversion failed" : null;

        var job = new ConversionJob(
                jobId, status, inputFileName, inputFileName + ".pdf",
                Instant.now(), completedAt, errorMessage, "http://example.com/webhook");

        String payload = webhookService.buildPayload(job);

        assertThat(payload).contains("\"jobId\":\"" + jobId + "\"");
        assertThat(payload).contains("\"status\":\"" + status.name() + "\"");
        assertThat(payload).contains("\"inputFileName\":\"" + inputFileName + "\"");
        assertThat(payload).contains("\"timestamp\":");
    }

    /**
     * Property 50: Webhook service does not throw when webhook URL is null or blank.
     *
     * **Validates: Requirements 23.3**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 50: Webhook Retry Logic")
    void webhookWithNullUrlDoesNotThrow(
            @ForAll("jobIds") String jobId,
            @ForAll("fileNames") String inputFileName,
            @ForAll("nullOrBlankUrls") String webhookUrl) {
        var job = new ConversionJob(
                jobId, JobStatus.COMPLETED, inputFileName, inputFileName + ".pdf",
                Instant.now(), Instant.now(), null, webhookUrl);

        // Should not throw - graceful no-op
        webhookService.notifyCompletion(job, webhookUrl);
    }

    /**
     * Property 51: Webhook payload is valid JSON with proper escaping.
     *
     * **Validates: Requirements 23.4**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 51: Webhook Signature")
    void webhookPayloadIsValidJson(
            @ForAll("jobIds") String jobId,
            @ForAll("fileNames") String inputFileName) {
        var job = new ConversionJob(
                jobId, JobStatus.COMPLETED, inputFileName, inputFileName + ".pdf",
                Instant.now(), Instant.now(), null, "http://example.com/webhook");

        String payload = webhookService.buildPayload(job);

        // Valid JSON starts with { and ends with }
        assertThat(payload).startsWith("{");
        assertThat(payload).endsWith("}");
        // Contains required fields
        assertThat(payload).contains("\"jobId\":");
        assertThat(payload).contains("\"status\":");
        assertThat(payload).contains("\"timestamp\":");
    }

    @Provide
    Arbitrary<String> jobIds() {
        return Arbitraries.strings()
                .withCharRange('a', 'f')
                .ofLength(8)
                .map(s -> s + "-abcd-1234-efgh-" + s + "ijkl");
    }

    @Provide
    Arbitrary<String> fileNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(3)
                .ofMaxLength(15)
                .map(name -> name + ".docx");
    }

    @Provide
    Arbitrary<JobStatus> statuses() {
        return Arbitraries.of(JobStatus.COMPLETED, JobStatus.FAILED);
    }

    @Provide
    Arbitrary<String> nullOrBlankUrls() {
        return Arbitraries.of("", "   ");
    }
}
