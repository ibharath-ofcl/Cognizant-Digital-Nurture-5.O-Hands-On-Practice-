package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Global reactive filter for logging details of all incoming requests processed by Spring Cloud Gateway.
 * Implements {@link GlobalFilter} to intercept all route traffic.
 * Implements {@link Ordered} to dictate execution precedence relative to other filters.
 */
@Component
public class LogFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);

    /**
     * Intercepts incoming client request, retrieves details, logs them, and forwards the chain.
     *
     * @param exchange current server web exchange containing request and response
     * @param chain standard gateway filter chain delegation
     * @return Mono indicating completion of processing
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String method = request.getMethod().name();
        HttpHeaders headers = request.getHeaders();

        logger.info("================ Incoming Request Log ================");
        logger.info("Request Path/URI : {}", uri);
        logger.info("HTTP Method      : {}", method);
        logger.info("Headers          : ");
        headers.forEach((key, values) -> {
            logger.info("  {}: {}", key, String.join(", ", values));
        });
        logger.info("======================================================");

        // Continue processing through the downstream filters
        return chain.filter(exchange);
    }

    /**
     * Defines the order of the filter execution. Lower values denote higher precedence.
     * Returning Ordered.HIGHEST_PRECEDENCE ensures the request is logged first.
     *
     * @return filter execution order
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
