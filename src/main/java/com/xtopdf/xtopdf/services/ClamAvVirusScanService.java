package com.xtopdf.xtopdf.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * ClamAV-based virus scanning implementation.
 * Connects to ClamAV daemon via TCP using the INSTREAM protocol.
 * <p>
 * Protocol:
 * 1. Send "zINSTREAM\0" command
 * 2. Send file data in chunks: [4-byte big-endian length][chunk data]
 * 3. Send terminator: [4 zero bytes]
 * 4. Read response: "stream: OK\0" or "stream: {virus_name} FOUND\0"
 * <p>
 * Active only when xtopdf.virus-scan.enabled=true.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "xtopdf.virus-scan.enabled", havingValue = "true")
public class ClamAvVirusScanService implements VirusScanService {

    private static final int CHUNK_SIZE = 2048;
    private static final byte[] INSTREAM_COMMAND = "zINSTREAM\0".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] ZERO_TERMINATOR = new byte[]{0, 0, 0, 0};

    private final String host;
    private final int port;
    private final int timeoutMs;

    public ClamAvVirusScanService(
            @Value("${xtopdf.virus-scan.host:localhost}") String host,
            @Value("${xtopdf.virus-scan.port:3310}") int port,
            @Value("${xtopdf.virus-scan.timeout-ms:30000}") int timeoutMs) {
        this.host = host;
        this.port = port;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public ScanResult scan(MultipartFile file) {
        log.info("Scanning file with ClamAV: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

        try (Socket socket = createSocket()) {
            socket.setSoTimeout(timeoutMs);

            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Send INSTREAM command
            out.write(INSTREAM_COMMAND);
            out.flush();

            // Stream file data in chunks
            try (InputStream fileStream = file.getInputStream()) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    // Send chunk length as 4-byte big-endian
                    out.write(ByteBuffer.allocate(4).putInt(bytesRead).array());
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Send zero-length terminator
            out.write(ZERO_TERMINATOR);
            out.flush();

            // Read response
            String response = readResponse(in);
            log.debug("ClamAV response: {}", response);

            if (response.contains("OK")) {
                return ScanResult.ok();
            } else if (response.contains("FOUND")) {
                String virusName = extractVirusName(response);
                log.warn("Virus detected in file {}: {}", file.getOriginalFilename(), virusName);
                return ScanResult.infected(virusName);
            } else {
                log.warn("Unexpected ClamAV response for file {}: {}",
                        file.getOriginalFilename(), response);
                // Fail-safe: reject file on unexpected response
                return ScanResult.infected("Scan inconclusive: " + response);
            }

        } catch (IOException e) {
            log.error("Failed to connect to ClamAV at {}:{} — {}", host, port, e.getMessage());
            // Fail-safe: if ClamAV is unavailable, reject the file
            return ScanResult.infected("Virus scan unavailable");
        }
    }

    /**
     * Creates a TCP socket connection to ClamAV.
     * Protected for testability.
     */
    protected Socket createSocket() throws IOException {
        return new Socket(host, port);
    }

    private String readResponse(InputStream in) throws IOException {
        var baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
            // ClamAV terminates response with null byte
            if (bytesRead > 0 && buffer[bytesRead - 1] == 0) {
                break;
            }
        }
        return baos.toString(StandardCharsets.US_ASCII).trim();
    }

    private String extractVirusName(String response) {
        // Response format: "stream: Win.Test.EICAR_HDB-1 FOUND"
        String cleaned = response.replace("stream:", "").replace("FOUND", "").trim();
        return cleaned.isEmpty() ? "Unknown virus" : cleaned;
    }
}
