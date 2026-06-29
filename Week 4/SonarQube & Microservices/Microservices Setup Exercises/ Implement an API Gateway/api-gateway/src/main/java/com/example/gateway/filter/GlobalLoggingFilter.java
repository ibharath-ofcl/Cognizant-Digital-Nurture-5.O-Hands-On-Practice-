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

import java.time.Instant;

/**
 * Global API Gateway logging filter.
 * Intercepts all traffic pre-execution (request entry) and post-execution (response exit).
 */
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);

    /**
     * Main filtering logic. Logs request details, then uses Mono.fromRunnable to run post-execution logic
     * once the downstream response is returned.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Retrieve the reactive server request
        ServerHttpRequest request = exchange.getRequest();
        
        // Capture the start timestamp of the request
        Instant startTime = Instant.now();
        
        // Log pre-filter request metadata at INFO level
        log.info(">>> [Gateway Request] Method: {} | URI: {} | Time: {}", 
                 request.getMethod(), request.getURI(), startTime);
        
        // Log individual request headers at DEBUG level for audit purposes
        request.getHeaders().forEach((headerName, headerValues) -> 
            log.debug("Header: {} = {}", headerName, String.join(", ", headerValues))
        );

        // Execute downstream filters in the chain and add post-filtering callback using Mono.then()
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Retrieve the reactive server response
            ServerHttpResponse response = exchange.getResponse();
            
            // Capture the completion timestamp
            Instant endTime = Instant.now();
            
            // Compute duration in milliseconds
            long duration = endTime.toEpochMilli() - startTime.toEpochMilli();
            
            // Log post-filter response status and calculated execution time at INFO level
            log.info("<<< [Gateway Response] URI: {} | Status: {} | Execution Time: {} ms", 
                     request.getURI(), response.getStatusCode(), duration);
        }));
    }

    /**
     * Specifies the order of filter execution.
     * HIGHEST_PRECEDENCE ensures this logging filter surrounds all other filters in the gateway.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
