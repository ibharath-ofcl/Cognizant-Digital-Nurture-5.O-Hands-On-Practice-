package com.cognizant.oauth2resourceserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class to bootstrap the OAuth2 Resource Server.
 */
@SpringBootApplication
public class OAuth2ResourceServerApplication {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ResourceServerApplication.class);

    public static void main(String[] args) {
        log.info("Bootstrapping the OAuth2 Resource Server Application...");
        SpringApplication.run(OAuth2ResourceServerApplication.class, args);
        log.info("OAuth2 Resource Server Application is running and listening for secure requests.");
    }
}
