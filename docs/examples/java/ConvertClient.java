package com.example.xtopdf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * XToPDF API - Java HttpClient Usage Examples
 *
 * Requirements:
 *   Java 11+ (java.net.http.HttpClient)
 *
 * Compile:
 *   javac ConvertClient.java
 *
 * Run:
 *   java ConvertClient
 */
public class ConvertClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Convert a single file to PDF.
     */
    public static byte[] basicConvert(Path filePath, String outputName) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();

        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();

        byte[] body = buildMultipartBody(boundary, "file", fileName, fileBytes);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/convert?outputFileName=" + outputName))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofMinutes(5))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            System.out.printf("Conversion successful: %d bytes%n", response.body().length);
            return response.body();
        } else {
            throw new RuntimeException("Conversion failed: HTTP " + response.statusCode());
        }
    }

    /**
     * Submit an async conversion job and poll for completion.
     */
    public static byte[] asyncConvert(Path filePath) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();
        byte[] fileBytes = Files.readAllBytes(filePath);
        String fileName = filePath.getFileName().toString();

        byte[] body = buildMultipartBody(boundary, "file", fileName, fileBytes);

        // Submit job
        HttpRequest submitRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/convert/async"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> submitResponse = client.send(submitRequest, HttpResponse.BodyHandlers.ofString());

        if (submitResponse.statusCode() != 202) {
            throw new RuntimeException("Job submission failed: HTTP " + submitResponse.statusCode());
        }

        // Extract jobId (simple JSON parsing without dependencies)
        String responseBody = submitResponse.body();
        String jobId = extractJsonValue(responseBody, "jobId");
        System.out.println("Job submitted: " + jobId);

        // Poll for completion
        while (true) {
            HttpRequest statusRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/convert/async/" + jobId))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> statusResponse = client.send(statusRequest, HttpResponse.BodyHandlers.ofString());
            String status = extractJsonValue(statusResponse.body(), "status");
            System.out.println("  Status: " + status);

            if ("COMPLETED".equals(status)) {
                // Download result
                HttpRequest resultRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/api/convert/async/" + jobId + "/result"))
                        .GET()
                        .timeout(Duration.ofMinutes(5))
                        .build();

                HttpResponse<byte[]> resultResponse = client.send(resultRequest, HttpResponse.BodyHandlers.ofByteArray());
                System.out.printf("Result downloaded: %d bytes%n", resultResponse.body().length);
                return resultResponse.body();
            }

            if ("FAILED".equals(status)) {
                throw new RuntimeException("Job failed: " + extractJsonValue(statusResponse.body(), "errorMessage"));
            }

            Thread.sleep(2000);
        }
    }

    /**
     * Add a watermark to an existing PDF.
     */
    public static byte[] addWatermark(Path pdfPath, String text, String layer) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();
        byte[] fileBytes = Files.readAllBytes(pdfPath);
        String fileName = pdfPath.getFileName().toString();

        byte[] body = buildMultipartBody(boundary, "file", fileName, fileBytes);

        String params = String.format("?text=%s&layer=%s", text, layer);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/pdf/watermark" + params))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofMinutes(2))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            System.out.printf("Watermark added: %d bytes%n", response.body().length);
            return response.body();
        } else {
            throw new RuntimeException("Watermark failed: HTTP " + response.statusCode());
        }
    }

    /**
     * Add page numbers to an existing PDF.
     */
    public static byte[] addPageNumbers(Path pdfPath, String position, String alignment, String style)
            throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();
        byte[] fileBytes = Files.readAllBytes(pdfPath);
        String fileName = pdfPath.getFileName().toString();

        byte[] body = buildMultipartBody(boundary, "file", fileName, fileBytes);

        String params = String.format("?position=%s&alignment=%s&style=%s", position, alignment, style);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/pdf/page-numbers" + params))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .timeout(Duration.ofMinutes(2))
                .build();

        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            System.out.printf("Page numbers added: %d bytes%n", response.body().length);
            return response.body();
        } else {
            throw new RuntimeException("Page numbers failed: HTTP " + response.statusCode());
        }
    }

    // --- Helpers ---

    private static byte[] buildMultipartBody(String boundary, String fieldName, String fileName, byte[] fileContent) {
        String prefix = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n";
        String suffix = "\r\n--" + boundary + "--\r\n";

        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        byte[] suffixBytes = suffix.getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[prefixBytes.length + fileContent.length + suffixBytes.length];
        System.arraycopy(prefixBytes, 0, body, 0, prefixBytes.length);
        System.arraycopy(fileContent, 0, body, prefixBytes.length, fileContent.length);
        System.arraycopy(suffixBytes, 0, body, prefixBytes.length + fileContent.length, suffixBytes.length);
        return body;
    }

    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIdx = json.indexOf(searchKey);
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx);
        int valueStart = json.indexOf("\"", colonIdx + 1);
        int valueEnd = json.indexOf("\"", valueStart + 1);
        return json.substring(valueStart + 1, valueEnd);
    }

    public static void main(String[] args) {
        System.out.println("=== XToPDF Java Examples ===\n");

        System.out.println("1. Basic conversion:");
        System.out.println("   byte[] pdf = ConvertClient.basicConvert(Path.of(\"document.docx\"), \"output.pdf\");\n");

        System.out.println("2. Async conversion:");
        System.out.println("   byte[] pdf = ConvertClient.asyncConvert(Path.of(\"large-file.xlsx\"));\n");

        System.out.println("3. Add watermark:");
        System.out.println("   byte[] pdf = ConvertClient.addWatermark(Path.of(\"doc.pdf\"), \"DRAFT\", \"FOREGROUND\");\n");

        System.out.println("4. Add page numbers:");
        System.out.println("   byte[] pdf = ConvertClient.addPageNumbers(Path.of(\"doc.pdf\"), \"BOTTOM\", \"CENTER\", \"ARABIC\");\n");

        System.out.println("See source code for full implementation details.");
    }
}
