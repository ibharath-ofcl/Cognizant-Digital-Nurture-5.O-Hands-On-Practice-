package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Gateway Filter Factory that implements the Token Bucket Rate Limiting algorithm in-memory.
 * Limit rules: 5 requests per minute (configurable via YAML parameters).
 */
@Component
public class CustomRateLimiterGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<CustomRateLimiterGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(CustomRateLimiterGatewayFilterFactory.class);

    // Thread-safe map storing the rate limiter token buckets associated with each client IP address.
    private final Map<String, TokenBucket> ipBuckets = new ConcurrentHashMap<>();

    public CustomRateLimiterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Resolve the client's IP address. Default to "unknown" if null.
            String clientIp = "unknown";
            if (exchange.getRequest().getRemoteAddress() != null) {
                clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                        .getAddress()
                        .getHostAddress();
            }

            // Fetch or create the client-specific token bucket
            TokenBucket bucket = ipBuckets.computeIfAbsent(clientIp, ip -> 
                new TokenBucket(config.getCapacity(), config.getReplenishRate(), config.getPeriodSeconds())
            );

            // Attempt to consume 1 token for the incoming request
            if (bucket.tryConsume()) {
                log.debug("Rate Limiter: Request allowed for IP: {}. Available tokens: {}", clientIp, bucket.getTokens());
                return chain.filter(exchange);
            } else {
                log.warn("Rate Limiter: Request rejected. Rate limit exceeded for IP: {}", clientIp);
                
                // Return a structured JSON response with HTTP 429 Too Many Requests
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                String errorJson = "{"
                        + "\"status\": 429,"
                        + "\"error\": \"Too Many Requests\","
                        + "\"message\": \"Rate limit exceeded. You are limited to 5 requests per minute.\""
                        + "}";
                
                DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
                return response.writeWith(Mono.just(buffer));
            }
        };
    }

    /**
     * Configuration class for mapping YAML arguments to the filter factory.
     */
    public static class Config {
        private int capacity = 5;          // Maximum bucket capacity (burst)
        private int replenishRate = 5;     // Number of tokens refilled per period
        private int periodSeconds = 60;    // Time window in seconds (default: 1 minute)

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
        }

        public int getPeriodSeconds() {
            return periodSeconds;
        }

        public void setPeriodSeconds(int periodSeconds) {
            this.periodSeconds = periodSeconds;
        }
    }

    /**
     * Inner class representing the Token Bucket structure.
     */
    private static class TokenBucket {
        private final double capacity;
        private final double replenishRate;
        private final long periodMs;
        
        private double tokens;
        private long lastRefillTimestamp;

        public TokenBucket(double capacity, double replenishRate, long periodSeconds) {
            this.capacity = capacity;
            this.replenishRate = replenishRate;
            this.periodMs = periodSeconds * 1000L;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        /**
         * Thread-safe check and consumption of a token.
         */
        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        /**
         * Refills tokens based on time elapsed since the last request.
         */
        private void refill() {
            long now = System.currentTimeMillis();
            long elapsedMs = now - lastRefillTimestamp;
            if (elapsedMs > 0) {
                double tokensToAdd = ((double) elapsedMs / periodMs) * replenishRate;
                if (tokensToAdd > 0) {
                    tokens = Math.min(capacity, tokens + tokensToAdd);
                    lastRefillTimestamp = now;
                }
            }
        }

        public synchronized double getTokens() {
            refill();
            return tokens;
        }
    }
}
