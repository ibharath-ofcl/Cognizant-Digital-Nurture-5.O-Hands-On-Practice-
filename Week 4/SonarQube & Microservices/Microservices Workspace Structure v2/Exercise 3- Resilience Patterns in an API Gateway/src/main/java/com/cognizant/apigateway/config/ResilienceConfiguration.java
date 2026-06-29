package com.cognizant.apigateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class to set up default Resilience4j parameters for the API Gateway.
 * It provides default fallback options for Circuit Breakers and Time Limiters.
 */
@Configuration
public class ResilienceConfiguration {

    /**
     * Configures the Reactive Resilience4J Circuit Breaker factory with default settings.
     * This Customizer is applied to all Circuit Breakers created by the factory when no explicit individual config exists.
     *
     * @return a Customizer for ReactiveResilience4JCircuitBreakerFactory
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                // Configure default Circuit Breaker parameters:
                // - Sliding window size: 100
                // - Failure rate threshold: 50%
                // - Slow call rate threshold: 100%
                // - Slow call duration threshold: 60000 ms (60s)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                // Configure default Time Limiter parameters:
                // - Timeout duration: 1 second
                // - Cancel running future: true
                .timeLimiterConfig(TimeLimiterConfig.ofDefaults())
                .build());
    }
}
