package com.cognizant.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Main application class for the API Gateway with Resilience patterns.
 * Exposes a fallback controller to handle circuit breaker and time limiter events gracefully.
 */
@SpringBootApplication
@RestController
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Fallback endpoint that provides a friendly JSON response when the downstream service
     * is down, returns errors, or times out.
     *
     * @return a reactive Mono wrapping a map with the error details.
     */
    @GetMapping("/fallback")
    public Mono<Map<String, Object>> fallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("message", "The downstream service is currently unavailable, experiencing high latency, or failing. Please try again later.");
        response.put("code", 503);
        response.put("patternApplied", "Resilience4j Circuit Breaker / Time Limiter");
        return Mono.just(response);
    }
}
