package com.example.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class to bootstrap the Payment Service.
 * @SpringBootApplication marks this class as a configuration, enables component scanning,
 * and configures auto-configuration (like Spring MVC, Actuator, and AOP).
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
