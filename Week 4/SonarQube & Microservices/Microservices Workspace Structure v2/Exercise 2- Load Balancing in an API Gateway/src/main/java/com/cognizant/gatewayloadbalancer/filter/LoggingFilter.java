package com.cognizant.gatewayloadbalancer.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

/**
 * Global Logging Filter to log Gateway routing decisions, processing time,
 * HTTP methods, paths, and selected service instances.
 * Implements Ordered interface to control execution order.
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    // Key to store the request start time in exchange attributes
    private static final String START_TIME_KEY = "request_start_time";

    /**
     * Filters the exchange, logs incoming request parameters, and registers
     * a post-callback to log routing result and duration.
     *
     * @param exchange the current server exchange
     * @param chain the gateway filter chain
     * @return Mono<Void> indicating completion
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI requestUri = request.getURI();
        String method = request.getMethod().name();

        // 1. Pre-routing phase: log incoming request details
        log.info("Incoming Request: Method={} Path={}", method, requestUri.getPath());
        exchange.getAttributes().put(START_TIME_KEY, System.currentTimeMillis());

        // 2. Proceed with the chain and perform logging in the post-routing phase
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME_KEY);
            long duration = 0;
            if (startTime != null) {
                duration = System.currentTimeMillis() - startTime;
            }

            ServerHttpResponse response = exchange.getResponse();
            HttpStatus statusCode = (HttpStatus) response.getStatusCode();
            int rawStatusCode = statusCode != null ? statusCode.value() : 500;

            // Retrieve the selected service instance URI from gateway attributes
            // ReactiveLoadBalancerClientFilter resolves the 'lb://service' to a physical URL
            URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            String selectedInstance = Optional.ofNullable(routeUri)
                    .map(URI::toString)
                    .orElse("Unknown (Not Routed)");

            // Log routing results, selected service instance, response status, and duration
            log.info("Outgoing Response: Method={} Path={} -> TargetInstance={} Status={} Duration={}ms",
                    method, requestUri.getPath(), selectedInstance, rawStatusCode, duration);
        }));
    }

    /**
     * Determines filter order. Must execute AFTER ReactiveLoadBalancerClientFilter (order 10150)
     * to ensure the load balancer has resolved the target URI.
     *
     * @return filter execution order
     */
    @Override
    public int getOrder() {
        // ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER is 10150.
        // Running at 10160 guarantees the load balancer has run and rewritten GATEWAY_REQUEST_URL_ATTR.
        return 10160;
    }
}
