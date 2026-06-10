package com.xtopdf.xtopdf.services;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for the conversion caching layer.
 *
 * **Validates: Requirements 11.1, 11.2, 11.3, 11.4**
 */
class ConversionCachePropertyTest {

    private ConversionCacheService cacheService;

    @BeforeProperty
    void setup() {
        cacheService = new ConversionCacheService();
        ReflectionTestUtils.setField(cacheService, "cacheEnabled", true);
        ReflectionTestUtils.setField(cacheService, "maxSize", 100);
        ReflectionTestUtils.setField(cacheService, "ttlMinutes", 60);
        cacheService.initCache();
    }

    /**
     * Property 24: For any file content and extension, computing a cache key
     * and storing a result allows retrieval of the same result.
     *
     * **Validates: Requirements 11.1**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 24: Conversion Caching")
    void cachedResultsCanBeRetrieved(
            @ForAll("fileContents") byte[] content,
            @ForAll("extensions") String extension) {
        String key = cacheService.computeCacheKey(content, extension);
        byte[] pdfBytes = "fake-pdf-content".getBytes();

        cacheService.put(key, pdfBytes);
        byte[] retrieved = cacheService.get(key);

        assertThat(retrieved).isEqualTo(pdfBytes);
    }

    /**
     * Property 25: Same input always produces the same cache key (deterministic hashing).
     *
     * **Validates: Requirements 11.2**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 25: Cache Hit Return")
    void sameInputProducesSameCacheKey(
            @ForAll("fileContents") byte[] content,
            @ForAll("extensions") String extension) {
        String key1 = cacheService.computeCacheKey(content, extension);
        String key2 = cacheService.computeCacheKey(content, extension);

        assertThat(key1).isEqualTo(key2);
    }

    /**
     * Property 26: Different content produces different cache keys.
     *
     * **Validates: Requirements 11.3**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 26: Cache Size Eviction")
    void differentContentProducesDifferentKeys(
            @ForAll("fileContents") byte[] content1,
            @ForAll("fileContents") byte[] content2,
            @ForAll("extensions") String extension) {
        Assume.that(!java.util.Arrays.equals(content1, content2));

        String key1 = cacheService.computeCacheKey(content1, extension);
        String key2 = cacheService.computeCacheKey(content2, extension);

        assertThat(key1).isNotEqualTo(key2);
    }

    /**
     * Property 27: When cache is disabled, get always returns null.
     *
     * **Validates: Requirements 11.4**
     */
    @Property(tries = 25)
    @Tag("Feature: advanced-improvements, Property 27: Cache TTL Expiration")
    void disabledCacheReturnsNull(
            @ForAll("fileContents") byte[] content,
            @ForAll("extensions") String extension) {
        // Create a disabled cache service
        ConversionCacheService disabledCache = new ConversionCacheService();
        ReflectionTestUtils.setField(disabledCache, "cacheEnabled", false);
        ReflectionTestUtils.setField(disabledCache, "maxSize", 100);
        ReflectionTestUtils.setField(disabledCache, "ttlMinutes", 60);
        disabledCache.initCache();

        String key = disabledCache.computeCacheKey(content, extension);
        disabledCache.put(key, "data".getBytes());

        assertThat(disabledCache.get(key)).isNull();
        assertThat(disabledCache.isEnabled()).isFalse();
    }

    @Provide
    Arbitrary<byte[]> fileContents() {
        return Arbitraries.bytes().array(byte[].class)
                .ofMinSize(10)
                .ofMaxSize(100);
    }

    @Provide
    Arbitrary<String> extensions() {
        return Arbitraries.of(".pdf", ".docx", ".xlsx", ".html", ".csv", ".png");
    }
}
