package com.example.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class to enable and configure Caffeine caching.
 * Caches customer responses in memory to optimize downstream request execution.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * CacheManager bean to manage application caches.
     * We register 'customerCache' and apply eviction and sizing rules.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(List.of("customerCache"));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Builds Caffeine cache builder specifying:
     * - Initial capacity of 100 entries.
     * - Maximum size of 500 entries (LRU eviction starts when size exceeds this limit).
     * - Expiration: entries expire 10 minutes after writing (TTL).
     * - Stats recording for metrics.
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
