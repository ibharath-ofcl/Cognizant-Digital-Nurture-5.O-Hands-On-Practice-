package com.example.greet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle greetings. Exposes REST endpoint for client consumption.
 */
@RestController
public class GreetController {

    private static final Logger log = LoggerFactory.getLogger(GreetController.class);

    /**
     * Responds with a greeting message.
     * Logs the incoming request details.
     *
     * @return greeting message string
     */
    @GetMapping("/greet")
    public String greet() {
        log.info("GreetController: Received request on /greet endpoint.");
        return "Hello World";
    }
}
