package com.xtopdf.xtopdf.services;

import com.xtopdf.xtopdf.dto.ConversionJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
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
     * Note: Retry loop blocks the calling virtual thread. For production with high
     * webhook volume, consider moving to an async message queue (e.g., Spring Events + @Async).
     *
     * @param job        the conversion job that completed
     * @param webhookUrl the URL to POST the notification to
     */
    public void notifyCompletion(ConversionJob job, String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        if (!isUrlSafe(webhookUrl)) {
            log.warn("Webhook URL rejected (unsafe destination): {}", webhookUrl);
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

    // KNOWN LIMITATION: DNS rebinding attacks are not fully mitigated.
    // The hostname is resolved once for validation but HttpClient may resolve again.
    // For production deployments with untrusted webhook URLs, consider:
    // 1. Using a webhook allowlist instead of blocklist
    // 2. Running webhook delivery through a forward proxy
    // 3. Implementing pinned DNS resolution
    /**
     * Validates that a webhook URL is safe to call (no SSRF).
     * Rejects private, loopback, and link-local addresses.
     */
    private boolean isUrlSafe(String url) {
        try {
            var uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            String hostLower = host.toLowerCase();
            if (hostLower.equals("localhost") || hostLower.endsWith(".localhost")) {
                return false;
            }

            InetAddress address = InetAddress.getByName(host);
            if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress() || address.isAnyLocalAddress()) {
                return false;
            }

            // Reject IPv6 unique local addresses (fc00::/7)
            byte[] addr = address.getAddress();
            if (addr.length == 16 && (addr[0] & 0xFE) == 0xFC) {
                return false;
            }

            // Check for IPv4-mapped IPv6 addresses (e.g., ::ffff:10.0.0.1)
            if (address instanceof java.net.Inet6Address inet6) {
                byte[] addrBytes = inet6.getAddress();
                // IPv4-mapped IPv6: first 10 bytes zero, bytes 10-11 are 0xFF
                if (addrBytes[10] == (byte)0xFF && addrBytes[11] == (byte)0xFF) {
                    // Extract the embedded IPv4 address and re-check
                    byte[] ipv4Bytes = new byte[4];
                    System.arraycopy(addrBytes, 12, ipv4Bytes, 0, 4);
                    InetAddress ipv4 = InetAddress.getByAddress(ipv4Bytes);
                    if (ipv4.isLoopbackAddress() || ipv4.isLinkLocalAddress()
                            || ipv4.isSiteLocalAddress() || ipv4.isAnyLocalAddress()) {
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
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
