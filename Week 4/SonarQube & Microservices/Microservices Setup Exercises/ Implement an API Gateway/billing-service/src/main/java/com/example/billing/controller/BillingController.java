package com.example.billing.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for dummy Billing Service.
 */
@RestController
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);

    @GetMapping("/payment")
    public Map<String, Object> getPayment(@RequestHeader Map<String, String> headers) {
        log.info("Received request for /payment");
        headers.forEach((key, value) -> log.debug("Header: {} = {}", key, value));

        return Map.of(
            "invoice", "INV1001",
            "amount", 5000,
            "status", "Paid"
        );
    }
}
