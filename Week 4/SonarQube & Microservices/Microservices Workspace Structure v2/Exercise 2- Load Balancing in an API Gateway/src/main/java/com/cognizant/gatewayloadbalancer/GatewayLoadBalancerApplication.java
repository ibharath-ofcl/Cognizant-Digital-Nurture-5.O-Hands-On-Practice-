package com.cognizant.gatewayloadbalancer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import com.cognizant.gatewayloadbalancer.loadbalancer.LoadBalancerConfiguration;

/**
 * Main application entry point for the API Gateway Load Balancer Service.
 * Implements standard Spring Boot 3 startup and shutdown hooks using SLF4J logger.
 */
@LoadBalancerClients(defaultConfiguration = LoadBalancerConfiguration.class)
@SpringBootApplication
public class GatewayLoadBalancerApplication {

    private static final Logger log = LoggerFactory.getLogger(GatewayLoadBalancerApplication.class);

    public static void main(String[] args) {
        log.info("Starting ApiGatewayLoadBalancer Application context initialization...");
        SpringApplication.run(GatewayLoadBalancerApplication.class, args);
    }

    @PostConstruct
    public void init() {
        log.info("GatewayLoadBalancerApplication successfully initialized (PostConstruct). Ready to route traffic.");
    }

    @PreDestroy
    public void destroy() {
        log.info("GatewayLoadBalancerApplication is shutting down (PreDestroy). Releasing resources...");
    }

    @Bean
    public CommandLineRunner startupLogger() {
        return args -> log.info("ApiGatewayLoadBalancer is online and running on port 8080. Custom load balancing filters are active.");
    }
}
