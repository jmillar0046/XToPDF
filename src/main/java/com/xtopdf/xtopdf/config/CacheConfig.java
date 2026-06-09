package com.xtopdf.xtopdf.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${xtopdf.cache.max-size:100}")
    private int maxSize;

    @Value("${xtopdf.cache.ttl-minutes:60}")
    private int ttlMinutes;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("conversions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
