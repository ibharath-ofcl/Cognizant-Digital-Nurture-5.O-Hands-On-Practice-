package com.example.greetservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing REST endpoints for the greeting service.
 */
@RestController
public class GreetController {

    private static final Logger logger = LoggerFactory.getLogger(GreetController.class);

    /**
     * Endpoint to fetch the default greeting message.
     * Maps to GET /greet
     *
     * @return "Hello World" plain text message.
     */
    @GetMapping("/greet")
    public String greet() {
        logger.info("Received request on /greet endpoint");
        return "Hello World";
    }
}
