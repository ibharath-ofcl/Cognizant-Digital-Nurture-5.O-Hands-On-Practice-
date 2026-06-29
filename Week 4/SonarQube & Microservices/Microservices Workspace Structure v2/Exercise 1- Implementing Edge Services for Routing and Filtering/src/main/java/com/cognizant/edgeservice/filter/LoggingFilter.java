package com.cognizant.edgeservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter that intercepts all requests entering the API Gateway.
 * Logs key information such as Request URI, HTTP Method, Headers, and Response Time.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    // Using SLF4J Logger as requested instead of System.out.println()
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Capture request start time in milliseconds
        long startTime = System.currentTimeMillis();

        // 2. Retrieve request object from exchange
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        HttpHeaders headers = request.getHeaders();

        // 3. Log pre-route details
        LOGGER.info("[PRE-FILTER] Incoming Request - Method: {}, URI: {}, Headers: {}", method, path, headers);

        // 4. Continue filter chain execution and attach post-route callback
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 5. Retrieve response object from exchange
            ServerHttpResponse response = exchange.getResponse();
            
            // 6. Calculate request execution duration
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 7. Log post-route details including response status and duration
            LOGGER.info("[POST-FILTER] Outgoing Response - URI: {}, Status: {}, Execution Time: {} ms",
                    path, response.getStatusCode(), executionTime);
        }));
    }

    /**
     * Determines the execution order of this filter. 
     * Ordered.HIGHEST_PRECEDENCE ensures this runs first on request start and last on response completion.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
