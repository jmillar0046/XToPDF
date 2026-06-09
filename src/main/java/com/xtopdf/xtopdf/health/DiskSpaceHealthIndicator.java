package com.xtopdf.xtopdf.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Custom health indicator that checks available disk space in the output and temp directories.
 * Reports UP when space is above 20%, DOWN when below 10%, and warns between 10-20%.
 * 
 * Note: This is a custom indicator separate from Spring Boot's built-in DiskSpaceHealthIndicator,
 * as it checks the application-specific output and temp directories.
 */
@Component("xtopdfDiskSpace")
@Slf4j
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final double WARNING_THRESHOLD = 0.20;
    private static final double CRITICAL_THRESHOLD = 0.10;

    private final String outputDirectory;
    private final String tempDirectory;

    public DiskSpaceHealthIndicator(
            @Value("${xtopdf.output.directory:/safe/output/directory}") String outputDirectory,
            @Value("${xtopdf.temp.directory:${java.io.tmpdir}}") String tempDirectory) {
        this.outputDirectory = outputDirectory;
        this.tempDirectory = tempDirectory;
    }

    @Override
    public Health health() {
        var outputDir = new File(outputDirectory);
        var tempDir = new File(tempDirectory);

        var outputStatus = checkDiskSpace(outputDir, "output");
        var tempStatus = checkDiskSpace(tempDir, "temp");

        // Overall status is the worst of the two
        var overallStatus = worstStatus(outputStatus.status(), tempStatus.status());

        var builder = switch (overallStatus) {
            case UP -> Health.up();
            case WARNING -> Health.status("WARNING");
            case DOWN -> Health.down();
        };

        builder.withDetail("outputDirectory", outputStatus.toMap());
        builder.withDetail("tempDirectory", tempStatus.toMap());

        return builder.build();
    }

    DiskStatus checkDiskSpace(File directory, String name) {
        if (!directory.exists()) {
            return new DiskStatus(name, Status.DOWN, 0, 0, "Directory does not exist");
        }

        long totalSpace = directory.getTotalSpace();
        long freeSpace = directory.getUsableSpace();

        if (totalSpace == 0) {
            return new DiskStatus(name, Status.DOWN, 0, 0, "Unable to determine disk space");
        }

        double freePercentage = (double) freeSpace / totalSpace;

        if (freePercentage < CRITICAL_THRESHOLD) {
            return new DiskStatus(name, Status.DOWN, totalSpace, freeSpace,
                    String.format("Critical: only %.1f%% free space", freePercentage * 100));
        } else if (freePercentage < WARNING_THRESHOLD) {
            return new DiskStatus(name, Status.WARNING, totalSpace, freeSpace,
                    String.format("Warning: only %.1f%% free space", freePercentage * 100));
        }

        return new DiskStatus(name, Status.UP, totalSpace, freeSpace,
                String.format("%.1f%% free space available", freePercentage * 100));
    }

    private Status worstStatus(Status a, Status b) {
        if (a == Status.DOWN || b == Status.DOWN) return Status.DOWN;
        if (a == Status.WARNING || b == Status.WARNING) return Status.WARNING;
        return Status.UP;
    }

    enum Status {
        UP, WARNING, DOWN
    }

    record DiskStatus(String name, Status status, long totalBytes, long freeBytes, String message) {
        java.util.Map<String, Object> toMap() {
            return java.util.Map.of(
                    "status", status.name(),
                    "totalBytes", totalBytes,
                    "freeBytes", freeBytes,
                    "message", message
            );
        }
    }
}
