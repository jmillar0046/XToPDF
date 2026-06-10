package com.xtopdf.xtopdf.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

// TODO: Integrate into FileConversionService once file-based conversion is refactored to return bytes

/**
 * Caching service for file conversion results.
 * Uses Caffeine in-memory cache with configurable TTL and max size.
 * Cache key is SHA-256 hash of file content + output extension.
 */
@Service
@Slf4j
public class ConversionCacheService {

    private Cache<String, byte[]> cache;

    @Value("${xtopdf.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${xtopdf.cache.max-size:100}")
    private int maxSize;

    @Value("${xtopdf.cache.ttl-minutes:60}")
    private int ttlMinutes;

    @PostConstruct
    void initCache() {
        cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();
        log.info("Conversion cache initialized: enabled={}, maxSize={}, ttlMinutes={}",
                cacheEnabled, maxSize, ttlMinutes);
    }

    /**
     * Computes the cache key from file content bytes and output extension.
     * Convenience method that passes null for optionsHash.
     *
     * @param fileContent the raw file bytes
     * @param outputExtension the target output extension (e.g., ".pdf")
     * @return SHA-256 hash as hex string
     */
    public String computeCacheKey(byte[] fileContent, String outputExtension) {
        return computeCacheKey(fileContent, outputExtension, null);
    }

    /**
     * Computes the cache key from file content bytes, output extension, and conversion options.
     *
     * @param fileContent the raw file bytes
     * @param outputExtension the target output extension (e.g., ".pdf")
     * @param optionsHash serialized conversion options string (nullable)
     * @return SHA-256 hash as hex string
     */
    public String computeCacheKey(byte[] fileContent, String outputExtension, String optionsHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(fileContent);
            digest.update(outputExtension.getBytes());
            if (optionsHash != null) {
                digest.update(optionsHash.getBytes());
            }
            byte[] hash = digest.digest();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Gets a cached conversion result by key.
     *
     * @param cacheKey the cache key
     * @return cached PDF bytes, or null if not cached
     */
    public byte[] get(String cacheKey) {
        if (!cacheEnabled) {
            return null;
        }
        byte[] result = cache.getIfPresent(cacheKey);
        if (result != null) {
            log.debug("Cache hit for key: {}", cacheKey.substring(0, 8));
        }
        return result;
    }

    /**
     * Stores a conversion result in the cache.
     *
     * @param cacheKey the cache key
     * @param pdfBytes the converted PDF bytes
     */
    public void put(String cacheKey, byte[] pdfBytes) {
        if (!cacheEnabled) {
            return;
        }
        cache.put(cacheKey, pdfBytes);
        log.debug("Cached result for key: {}", cacheKey.substring(0, 8));
    }

    /**
     * Checks if caching is enabled.
     */
    public boolean isEnabled() {
        return cacheEnabled;
    }

    /**
     * Returns the current cache size.
     */
    public long size() {
        return cache.estimatedSize();
    }

    /**
     * Invalidates all cache entries.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }
}
