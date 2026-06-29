package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;

/**
 * Global reactive filter that intercepts all incoming gateway requests,
 * logging details about requests and their execution time upon response.
 */
@Component
public class LogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Capture incoming request details
        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        Instant startTime = Instant.now();

        log.info("================== INCOMING REQUEST ==================");
        log.info("Timestamp: {}", startTime);
        log.info("URI      : {}", requestUri);
        log.info("Method   : {}", method);
        log.info("Headers  : {}", request.getHeaders());
        log.info("======================================================");

        // Continue processing request and intercept the response reactively
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Instant endTime = Instant.now();
            long executionTime = endTime.toEpochMilli() - startTime.toEpochMilli();
            ServerHttpResponse response = exchange.getResponse();
            
            log.info("================== OUTGOING RESPONSE ==================");
            log.info("Status Code   : {}", response.getStatusCode());
            log.info("Execution Time: {} ms", executionTime);
            log.info("=======================================================");
        }));
    }

    /**
     * Determines the order of filter execution.
     * Higher priority is given to filters with lower values.
     * Setting order to -1 ensures this runs early in the gateway chain.
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
