package com.xtopdf.xtopdf.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.health.contributor.Status;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DiskSpaceHealthIndicatorTest {

    @TempDir
    Path tempDir;

    @Test
    void healthReturnsUpWhenSufficientDiskSpace() {
        var indicator = new DiskSpaceHealthIndicator(
                tempDir.toString(), tempDir.toString());
        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKeys("outputDirectory", "tempDirectory");
    }

    @Test
    void healthReturnsDownWhenDirectoryDoesNotExist() {
        var indicator = new DiskSpaceHealthIndicator(
                "/nonexistent/path/that/should/not/exist",
                "/another/nonexistent/path");
        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    void checkDiskSpaceReturnsDownForNonexistentDirectory() {
        var indicator = new DiskSpaceHealthIndicator(
                tempDir.toString(), tempDir.toString());

        var result = indicator.checkDiskSpace(new File("/nonexistent/dir"), "test");

        assertThat(result.status()).isEqualTo(DiskSpaceHealthIndicator.Status.DOWN);
        assertThat(result.message()).contains("does not exist");
    }

    @Test
    void checkDiskSpaceReturnsUpForExistingDirectoryWithSpace() {
        var indicator = new DiskSpaceHealthIndicator(
                tempDir.toString(), tempDir.toString());

        var result = indicator.checkDiskSpace(tempDir.toFile(), "test");

        assertThat(result.status()).isEqualTo(DiskSpaceHealthIndicator.Status.UP);
        assertThat(result.totalBytes()).isGreaterThan(0);
        assertThat(result.freeBytes()).isGreaterThan(0);
    }

    @Test
    void diskStatusToMapContainsExpectedKeys() {
        var status = new DiskSpaceHealthIndicator.DiskStatus(
                "test", DiskSpaceHealthIndicator.Status.UP, 1000L, 500L, "50.0% free space available");

        var map = status.toMap();

        assertThat(map).containsEntry("status", "UP");
        assertThat(map).containsEntry("totalBytes", 1000L);
        assertThat(map).containsEntry("freeBytes", 500L);
        assertThat(map).containsEntry("message", "50.0% free space available");
    }
}
