package com.example.payment.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration class to define Spring beans.
 * @Configuration indicates that this class contains one or more bean definition methods.
 */
@Configuration
public class AppConfig {

    /**
     * Creates a configured RestTemplate instance using RestTemplateBuilder.
     * Connection timeout and Read timeout are set to 2 seconds.
     * Any third-party service call taking longer than 2 seconds will throw a timeout exception.
     * 
     * @param builder RestTemplateBuilder injected by Spring Boot
     * @return configured RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(2)) // 2 seconds connect timeout
                .setReadTimeout(Duration.ofSeconds(2))    // 2 seconds read timeout
                .build();
    }
}
