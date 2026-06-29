package com.example.customer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for dummy Customer Service.
 */
@RestController
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestHeader Map<String, String> headers) {
        log.info("Received request for /profile");
        headers.forEach((key, value) -> log.debug("Header: {} = {}", key, value));
        
        return Map.of(
            "id", 1,
            "name", "John",
            "city", "Chennai"
        );
    }
}
