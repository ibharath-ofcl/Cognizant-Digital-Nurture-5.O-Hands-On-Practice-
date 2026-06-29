package com.cognizant.gatewayloadbalancer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fallback Controller to serve default responses when downstream microservices
 * are offline or hit circuit breaker limits.
 * Demonstrates basic reactive responses using Mono.
 */
@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    /**
     * Fallback for the Employee service routing.
     *
     * @return Mono of ResponseEntity containing default error JSON payload.
     */
    @GetMapping("/fallback/employee")
    public Mono<ResponseEntity<Map<String, Object>>> employeeFallback() {
        log.warn("Fallback triggered: Employee service is unavailable or slow. Returning mock fallback payload.");
        return Mono.just(createFallbackResponse("Employee Service is currently unavailable. Please try again later."));
    }

    /**
     * Fallback for the Department service routing.
     *
     * @return Mono of ResponseEntity containing default error JSON payload.
     */
    @GetMapping("/fallback/department")
    public Mono<ResponseEntity<Map<String, Object>>> departmentFallback() {
        log.warn("Fallback triggered: Department service is unavailable or slow. Returning mock fallback payload.");
        return Mono.just(createFallbackResponse("Department Service is currently unavailable. Please try again later."));
    }

    /**
     * Helper to create a fallback response payload.
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String message) {
        Map<String, Object> fallbackDetails = new LinkedHashMap<>();
        fallbackDetails.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        fallbackDetails.put("error", "Service Unavailable");
        fallbackDetails.put("message", message);
        fallbackDetails.put("fallbackActive", true);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackDetails);
    }
}
