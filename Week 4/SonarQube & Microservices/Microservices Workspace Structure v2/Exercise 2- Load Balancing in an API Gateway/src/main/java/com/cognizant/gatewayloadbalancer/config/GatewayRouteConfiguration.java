package com.cognizant.gatewayloadbalancer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Java Configuration for API Gateway Routing.
 * Exposes routes programmatically using RouteLocatorBuilder, demonstrating
 * how programmatic configuration can co-exist and supplement application.yml.
 */
@Configuration
public class GatewayRouteConfiguration {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteConfiguration.class);

    /**
     * Programmatic Route Definition using Java DSL.
     * Routes requests starting with /java-employees/** to employee-service using
     * the load balancer scheme 'lb://', and routes requests starting with
     * /java-departments/** to department-service using 'lb://'.
     *
     * @param builder the RouteLocatorBuilder injected by Spring Boot
     * @return the RouteLocator defining programmatic routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("Registering Java-configured routes in API Gateway...");
        return builder.routes()
                // Route for load-balanced employee-service programmatically
                .route("java-load-balanced-route", r -> r
                        .path("/java-employees/**")
                        .filters(f -> f
                                .stripPrefix(1) // Strips '/java-employees' so downstream gets '/employees/**'
                                .addResponseHeader("X-Gateway-Route-Source", "Java-DSL")
                                .addResponseHeader("X-Gateway-Route-ID", "java-load-balanced-route")
                        )
                        .uri("lb://employee-service")
                )
                // Route for load-balanced department-service programmatically
                .route("java-department-route", r -> r
                        .path("/java-departments/**")
                        .filters(f -> f
                                .stripPrefix(1) // Strips '/java-departments' so downstream gets '/departments/**'
                                .addResponseHeader("X-Gateway-Route-Source", "Java-DSL")
                                .addResponseHeader("X-Gateway-Route-ID", "java-department-route")
                        )
                        .uri("lb://department-service")
                )
                .build();
    }
}
