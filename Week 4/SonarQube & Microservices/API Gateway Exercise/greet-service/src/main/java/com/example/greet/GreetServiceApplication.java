package com.example.greet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for Greet Service.
 * Annotated with {@code @EnableDiscoveryClient} to register as a client in the Eureka server.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GreetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetServiceApplication.class, args);
    }
}
