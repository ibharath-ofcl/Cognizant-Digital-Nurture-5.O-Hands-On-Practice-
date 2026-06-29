package com.example.payment.controller;

import com.example.payment.model.PaymentResponse;
import com.example.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing endpoints to clients.
 * 
 * Annotations explained:
 * @RestController: Marks this class as a REST Controller, combining @Controller and @ResponseBody.
 */
@RestController
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    // Manual constructor for dependency injection instead of Lombok's @RequiredArgsConstructor
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Entrypoint for clients to process payments.
     * Exposes GET /payment endpoint.
     *
     * Annotations explained:
     * @GetMapping("/payment"): Maps HTTP GET requests for "/payment" to this method.
     * @RequestParam(name = "mode", defaultValue = "success"): Binds the query parameter "mode" to the Java String.
     *
     * @param mode determines target behavior (success, slow, fail)
     * @return ResponseEntity containing PaymentResponse payload
     */
    @GetMapping("/payment")
    public ResponseEntity<PaymentResponse> pay(@RequestParam(name = "mode", defaultValue = "success") String mode) {
        log.info("PaymentController: [Incoming request] Received GET /payment request with parameter mode = {}", mode);
        
        long startTime = System.currentTimeMillis();
        PaymentResponse response = paymentService.processPayment(mode);
        long totalDuration = System.currentTimeMillis() - startTime;
        
        log.info("PaymentController: [Response status] Processing complete. Total time taken: {} ms. Status returned: {}", 
                totalDuration, response.getStatus());

        return ResponseEntity.ok(response);
    }
}
