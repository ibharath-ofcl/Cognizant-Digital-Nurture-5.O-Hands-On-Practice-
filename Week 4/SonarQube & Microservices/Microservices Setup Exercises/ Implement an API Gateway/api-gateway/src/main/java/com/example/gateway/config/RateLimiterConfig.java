package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Configuration class for Rate Limiting.
 * Configures the KeyResolver to identify clients uniquely (e.g. by remote IP address).
 */
@Configuration
public class RateLimiterConfig {

    /**
     * Resolves the remote client's Host IP Address to be used as the unique key
     * for limiting request counts.
     */
    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress()
                .getHostAddress()
        );
    }
}
