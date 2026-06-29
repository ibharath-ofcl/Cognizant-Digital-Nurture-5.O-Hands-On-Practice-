package com.example.payment.controller;

import com.example.payment.model.ThirdPartyPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller simulating an external Third-Party Payment provider.
 * Under standard microservice architectures, this would be an external API (like Stripe/PayPal).
 * 
 * Annotations explained:
 * @RestController: Marks the class as a web controller and adds @ResponseBody to serialize return values to JSON.
 * @RequestMapping: Defines the base path "/thirdparty" for all endpoints inside this class.
 */
@RestController
@RequestMapping("/thirdparty")
public class ThirdPartyPaymentController {

    private static final Logger log = LoggerFactory.getLogger(ThirdPartyPaymentController.class);

    /**
     * Simulates a slow payment response.
     * Artificially delays the execution by 5 seconds using Thread.sleep().
     * This will trigger read timeout exceptions in clients with timeouts shorter than 5 seconds.
     *
     * @return ThirdPartyPaymentResponse with status SUCCESS
     */
    @GetMapping("/pay")
    public ResponseEntity<ThirdPartyPaymentResponse> paySlow() {
        log.info("Incoming request to GET /thirdparty/pay (simulating SLOW API call)...");
        long startTime = System.currentTimeMillis();
        try {
            // Artificially delay for 5 seconds (5000 milliseconds)
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Thread was interrupted during sleep execution", e);
            Thread.currentThread().interrupt();
        }
        long duration = System.currentTimeMillis() - startTime;
        log.info("Simulated delay complete. Response prepared in {} ms. Returning SUCCESS.", duration);
        
        ThirdPartyPaymentResponse response = new ThirdPartyPaymentResponse("SUCCESS", "TXN1001", "ThirdPartyAPI");
        return ResponseEntity.ok(response);
    }

    /**
     * Simulates a rapid external api failure (database or infrastructure crash).
     * Throws a RuntimeException immediately.
     *
     * Useful for testing circuit breakers under failure conditions without forcing timeouts.
     */
    @GetMapping("/fail")
    public ResponseEntity<ThirdPartyPaymentResponse> payFail() {
        log.warn("Incoming request to GET /thirdparty/fail (simulating SYSTEM FAILURE)...");
        throw new RuntimeException("Third-party payment gateway is offline or database connection is down.");
    }

    /**
     * Simulates a fast and successful payment processing.
     * Returns a response immediately (no delay, no exceptions).
     * This is useful to test the normal flow and circuit breaker recovery back to CLOSED state.
     *
     * @return ThirdPartyPaymentResponse with status SUCCESS
     */
    @GetMapping("/success")
    public ResponseEntity<ThirdPartyPaymentResponse> paySuccess() {
        log.info("Incoming request to GET /thirdparty/success (simulating FAST SUCCESS API call)...");
        ThirdPartyPaymentResponse response = new ThirdPartyPaymentResponse("SUCCESS", "TXN1002", "ThirdPartyAPI");
        return ResponseEntity.ok(response);
    }
}
