package com.xtopdf.xtopdf.services;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for virus scanning uploaded files before conversion.
 * Implementations may integrate with external scanners (e.g., ClamAV)
 * or provide a no-op implementation when scanning is disabled.
 */
public interface VirusScanService {

    /**
     * Scan result indicating whether a file is clean or infected.
     *
     * @param clean   true if the file passed the scan
     * @param message human-readable result description
     */
    record ScanResult(boolean clean, String message) {
        public static ScanResult clean() {
            return new ScanResult(true, "OK");
        }

        public static ScanResult infected(String reason) {
            return new ScanResult(false, reason);
        }
    }

    /**
     * Scans the given file for viruses/malware.
     *
     * @param file the uploaded file to scan
     * @return the scan result
     */
    ScanResult scan(MultipartFile file);
}
