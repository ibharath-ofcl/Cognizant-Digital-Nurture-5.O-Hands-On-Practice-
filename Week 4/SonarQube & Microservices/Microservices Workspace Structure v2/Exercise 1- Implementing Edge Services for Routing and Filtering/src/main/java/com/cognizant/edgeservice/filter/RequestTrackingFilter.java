package com.cognizant.edgeservice.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Custom Gateway Filter that handles request tracking.
 * Generates a unique tracking ID, stores it in ServerWebExchange attributes, 
 * propagates it down the request chain, and appends it to the response headers.
 */
@Component
public class RequestTrackingFilter implements GatewayFilter, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestTrackingFilter.class);
    
    // Key used to store the Request ID in the exchange attributes map
    public static final String TRACKING_ID_KEY = "trackingId";
    
    // Header name for tracking propagation
    public static final String TRACKING_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Generate a unique Request ID (UUID)
        String trackingId = UUID.randomUUID().toString();
        
        // 2. Store the Request ID in Exchange attributes for reuse in down-stream logging / error handling
        exchange.getAttributes().put(TRACKING_ID_KEY, trackingId);
        
        LOGGER.debug("[TRACKING] Generated Request ID: {} for path: {}", trackingId, exchange.getRequest().getURI().getPath());

        // 3. Mutate the request to add the header before routing downstream
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TRACKING_HEADER, trackingId)
                .build();
        
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // 4. Proceed with the filter chain and hook into response stream to append the header to outgoing response
        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            LOGGER.debug("[TRACKING] Appending Request ID: {} to response header", trackingId);
            mutatedExchange.getResponse().getHeaders().add(TRACKING_HEADER, trackingId);
        }));
    }

    /**
     * Set high precedence to ensure tracking ID is generated before most downstream routing work.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // Executed right after LoggingFilter
    }
}
