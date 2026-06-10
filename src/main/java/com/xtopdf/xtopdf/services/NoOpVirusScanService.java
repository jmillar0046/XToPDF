package com.xtopdf.xtopdf.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * No-op virus scan implementation used when virus scanning is disabled.
 * Always returns a clean result without performing any actual scan.
 * This is the default when xtopdf.virus-scan.enabled=false (or not set).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "xtopdf.virus-scan.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpVirusScanService implements VirusScanService {

    @Override
    public ScanResult scan(MultipartFile file) {
        log.debug("Virus scanning disabled — skipping scan for file: {}",
                file.getOriginalFilename());
        return ScanResult.ok();
    }
}
