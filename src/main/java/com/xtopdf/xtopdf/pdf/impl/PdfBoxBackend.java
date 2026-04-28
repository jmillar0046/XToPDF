package com.xtopdf.xtopdf.pdf.impl;

import com.xtopdf.xtopdf.pdf.PdfBackendProvider;
import com.xtopdf.xtopdf.pdf.PdfDocumentBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Apache PDFBox backend provider implementation.
 * 
 * <p>This provider creates PDF document builders using Apache PDFBox,
 * which is licensed under Apache License 2.0 and suitable for commercial use.</p>
 * 
 * <p>Font files are loaded once at initialization and cached as byte arrays.
 * Each call to {@link #createBuilder()} passes the cached bytes to the builder
 * constructor, avoiding repeated classpath I/O.</p>
 */
@Slf4j
@Component("pdfboxBackend")
public class PdfBoxBackend implements PdfBackendProvider {

    private byte[] regularFontBytes;
    private byte[] boldFontBytes;
    private byte[] cjkFontBytes;

    public PdfBoxBackend() {
        loadFontBytes();
    }

    @PostConstruct
    void loadFontBytes() {
        if (regularFontBytes != null) {
            return; // Already loaded by constructor
        }
        regularFontBytes = loadResource("/fonts/NotoSans-Regular.ttf");
        boldFontBytes = loadResource("/fonts/NotoSans-Bold.ttf");
        cjkFontBytes = loadCjkResource();
        log.info("Font bytes cached: regular={}, bold={}, cjk={}",
                regularFontBytes != null ? regularFontBytes.length + " bytes" : "null",
                boldFontBytes != null ? boldFontBytes.length + " bytes" : "null",
                cjkFontBytes != null ? cjkFontBytes.length + " bytes" : "null");
    }

    @Override
    public PdfDocumentBuilder createBuilder() throws IOException {
        return new PdfBoxDocumentBuilder(regularFontBytes, boldFontBytes, cjkFontBytes);
    }
    
    @Override
    public String getBackendName() {
        return "pdfbox";
    }

    /**
     * Reads a classpath resource into a byte array.
     *
     * @param path the classpath resource path (e.g., "/fonts/NotoSans-Regular.ttf")
     * @return the raw bytes, or null if the resource cannot be read
     */
    private byte[] loadResource(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                log.warn("Font resource not found on classpath: {}", path);
                return null;
            }
            return is.readAllBytes();
        } catch (IOException e) {
            log.warn("Failed to read font resource {}: {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * Attempts to load the CJK font resource, trying OTF first, then TTF, then SC fallback.
     *
     * @return the raw bytes of the CJK font, or null if none could be loaded
     */
    private byte[] loadCjkResource() {
        byte[] bytes = loadResource("/fonts/NotoSansCJK-Regular.otf");
        if (bytes != null) {
            return bytes;
        }
        bytes = loadResource("/fonts/NotoSansCJK-Regular.ttf");
        if (bytes != null) {
            return bytes;
        }
        bytes = loadResource("/fonts/NotoSansSC-Regular.ttf");
        if (bytes != null) {
            return bytes;
        }
        log.warn("No CJK font resource could be loaded; CJK characters may not render correctly");
        return null;
    }

    // Package-private accessors for testing
    byte[] getRegularFontBytes() {
        return regularFontBytes;
    }

    byte[] getBoldFontBytes() {
        return boldFontBytes;
    }

    byte[] getCjkFontBytes() {
        return cjkFontBytes;
    }
}
