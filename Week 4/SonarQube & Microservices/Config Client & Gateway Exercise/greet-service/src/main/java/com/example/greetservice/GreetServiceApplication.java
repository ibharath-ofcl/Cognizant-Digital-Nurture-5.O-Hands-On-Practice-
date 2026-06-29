package com.example.greetservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for Greet Service.
 * Annotated with {@link EnableDiscoveryClient} to explicitly register as a client in the Service Registry.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GreetServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreetServiceApplication.class, args);
    }
}
