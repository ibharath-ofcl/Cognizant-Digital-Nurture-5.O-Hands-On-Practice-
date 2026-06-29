package com.cognizant.jwtauthentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for the Spring Boot 3 JWT Authentication application.
 */
@SpringBootApplication
public class JwtAuthenticationApplication {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationApplication.class);

    public static void main(String[] reconciliationArgs) {
        log.info("Starting JwtAuthenticationApplication...");
        SpringApplication.run(JwtAuthenticationApplication.class, reconciliationArgs);
        log.info("JwtAuthenticationApplication is running successfully.");
    }
}
