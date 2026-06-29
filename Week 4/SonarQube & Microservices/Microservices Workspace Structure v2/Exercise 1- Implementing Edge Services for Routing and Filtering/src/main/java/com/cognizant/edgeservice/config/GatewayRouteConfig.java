package com.cognizant.edgeservice.config;

import com.cognizant.edgeservice.filter.RequestTrackingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic routing configuration using Java DSL.
 * Demonstrates route definition via code, integrating predicates, filters,
 * and custom tracking filters dynamically.
 */
@Configuration
public class GatewayRouteConfig {

    private final RequestTrackingFilter requestTrackingFilter;

    // Injecting the custom RequestTrackingFilter using constructor injection
    public GatewayRouteConfig(RequestTrackingFilter requestTrackingFilter) {
        this.requestTrackingFilter = requestTrackingFilter;
    }

    /**
     * Defines programmatic routes for the Gateway.
     * Uses RouteLocatorBuilder to construct route rules fluently.
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route 1: Programmatic Route for Employee Service
                // Access URL: http://localhost:8080/java/employee/list -> Routes to http://localhost:8081/list
                .route("java_employee_route", r -> r
                        .path("/java/employee/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(requestTrackingFilter)
                                .addRequestHeader("X-Routed-By", "Java-DSL-Config")
                                .addResponseHeader("X-Java-Route-Status", "Processed")
                        )
                        .uri("http://localhost:8081")
                )
                // Route 2: Programmatic Route for Department Service
                // Access URL: http://localhost:8080/java/department/** -> Routes to http://localhost:8082/**
                .route("java_department_route", r -> r
                        .path("/java/department/**")
                        .and()
                        .method("GET", "POST")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(requestTrackingFilter)
                                .addRequestHeader("X-Service-Destination", "Department-Service")
                        )
                        .uri("http://localhost:8082")
                )
                .build();
    }
}
