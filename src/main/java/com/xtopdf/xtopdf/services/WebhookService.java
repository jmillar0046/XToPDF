package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.dto.ConversionJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * Service for sending HTTP POST webhook notifications when async jobs complete or fail.
 * Supports retry with exponential backoff.
 */
@Service
@Slf4j
public class WebhookService {

    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration INITIAL_BACKOFF = Duration.ofSeconds(1);

    private final HttpClient httpClient;

    public WebhookService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    /**
     * Constructor for testing with a custom HttpClient.
     */
    WebhookService(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sends a webhook notification for a completed/failed job.
     * Retries with exponential backoff on failure.
     *
     * @param job        the conversion job that completed
     * @param webhookUrl the URL to POST the notification to
     */
    public void notifyCompletion(ConversionJob job, String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        String payload = buildPayload(job);
        log.info("Sending webhook notification for job {} to {}", job.id(), webhookUrl);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 0) {
                    long backoffMs = INITIAL_BACKOFF.toMillis() * (long) Math.pow(2, attempt - 1);
                    Thread.sleep(backoffMs);
                    log.debug("Webhook retry attempt {} for job {} after {}ms backoff", attempt, job.id(), backoffMs);
                }

                var request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .timeout(TIMEOUT)
                        .header("Content-Type", "application/json")
                        .header("X-Webhook-Event", "job.completed")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("Webhook delivered successfully for job {} (status={})", job.id(), response.statusCode());
                    return;
                }

                log.warn("Webhook delivery returned non-success status {} for job {}", response.statusCode(), job.id());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Webhook delivery interrupted for job {}", job.id());
                return;
            } catch (Exception e) {
                log.warn("Webhook delivery failed for job {} (attempt {}): {}", job.id(), attempt + 1, e.getMessage());
            }
        }

        log.error("Webhook delivery exhausted retries for job {} to {}", job.id(), webhookUrl);
    }

    /**
     * Builds the JSON payload for the webhook notification.
     *
     * @param job the conversion job
     * @return JSON string payload
     */
    String buildPayload(ConversionJob job) {
        var sb = new StringBuilder();
        sb.append("{");
        sb.append("\"jobId\":\"").append(escapeJson(job.id())).append("\",");
        sb.append("\"status\":\"").append(job.status().name()).append("\",");
        sb.append("\"inputFileName\":\"").append(escapeJson(job.inputFileName())).append("\",");
        sb.append("\"outputFileName\":\"").append(escapeJson(job.outputFileName())).append("\",");
        sb.append("\"timestamp\":\"").append(Instant.now().toString()).append("\"");
        if (job.completedAt() != null) {
            sb.append(",\"completedAt\":\"").append(job.completedAt().toString()).append("\"");
        }
        if (job.errorMessage() != null) {
            sb.append(",\"errorMessage\":\"").append(escapeJson(job.errorMessage())).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
