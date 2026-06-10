package com.xtopdf.xtopdf.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initializer that ensures the output directory exists at startup.
 * Creates the directory (and parent directories) if they do not already exist.
 */
@Component
@Slf4j
public class OutputDirectoryInitializer {

    @Value("${xtopdf.output.directory:${java.io.tmpdir}/xtopdf-output}")
    private String outputDirectory;

    @PostConstruct
    void initOutputDirectory() {
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            try {
                Files.createDirectories(outputPath);
                log.info("Created output directory: {}", outputPath.toAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to create output directory: {}", outputPath.toAbsolutePath());
            }
        } else {
            log.info("Output directory already exists: {}", outputPath.toAbsolutePath());
        }
    }
}
