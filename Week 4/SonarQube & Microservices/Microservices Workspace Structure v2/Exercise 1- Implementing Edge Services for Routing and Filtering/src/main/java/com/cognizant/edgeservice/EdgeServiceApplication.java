package com.cognizant.edgeservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the Edge Service API Gateway.
 * Built using Spring Boot 3.4.1 and Spring Cloud Gateway 2024.0.0.
 */
@SpringBootApplication
public class EdgeServiceApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeServiceApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Edge Service Gateway...");
        SpringApplication.run(EdgeServiceApplication.class, args);
        LOGGER.info("Edge Service Gateway started successfully and is ready to route requests.");
    }
}
