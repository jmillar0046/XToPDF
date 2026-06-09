package com.xtopdf.xtopdf.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Scheduled cleanup of orphaned temporary files from PDF conversion operations.
 * 
 * <p>Periodically walks the configured temp directory and deletes files matching
 * known temporary file prefixes (pdf_*, merged_*, watermark_*) that are older
 * than the configured maximum age.</p>
 */
@Slf4j
@Component
public class TempFileCleanupScheduler {

    private static final String[] TEMP_PREFIXES = {"pdf_", "merged_", "watermark_"};

    private final String tempDirectory;
    private final int maxAgeMinutes;

    public TempFileCleanupScheduler(
            @Value("${xtopdf.temp.directory:${java.io.tmpdir}}") String tempDirectory,
            @Value("${xtopdf.temp.max-age-minutes:60}") int maxAgeMinutes) {
        this.tempDirectory = tempDirectory;
        this.maxAgeMinutes = maxAgeMinutes;
    }

    @Scheduled(fixedRateString = "${xtopdf.temp.cleanup-interval-minutes:15}", timeUnit = TimeUnit.MINUTES)
    public void cleanupOldTempFiles() {
        Path tempDir = Paths.get(tempDirectory);
        if (!Files.isDirectory(tempDir)) {
            log.warn("Temp directory does not exist: {}", tempDirectory);
            return;
        }

        Instant cutoff = Instant.now().minus(Duration.ofMinutes(maxAgeMinutes));
        int deletedCount = 0;

        try (Stream<Path> files = Files.list(tempDir)) {
            var fileList = files.toList();
            for (Path file : fileList) {
                if (Files.isRegularFile(file) && isTempFile(file) && isOlderThan(file, cutoff)) {
                    try {
                        Files.deleteIfExists(file);
                        deletedCount++;
                    } catch (IOException e) {
                        log.warn("Failed to delete temp file {}: {}", file, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error listing temp directory {}: {}", tempDirectory, e.getMessage(), e);
        }

        if (deletedCount > 0) {
            log.info("Cleaned up {} old temp files from {}", deletedCount, tempDirectory);
        }
    }

    private boolean isTempFile(Path file) {
        String fileName = file.getFileName().toString();
        for (String prefix : TEMP_PREFIXES) {
            if (fileName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOlderThan(Path file, Instant cutoff) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            return attrs.lastModifiedTime().toInstant().isBefore(cutoff);
        } catch (IOException e) {
            return false;
        }
    }
}
