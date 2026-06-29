package com.example.payment.service;

import com.example.payment.model.PaymentResponse;
import com.example.payment.model.ThirdPartyPaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service class implementing payment processing logic.
 * 
 * Annotations explained:
 * @Service: Marks this class as a Service component in the application context for dependency injection.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // Injecting the configured RestTemplate bean to execute HTTP requests
    private final RestTemplate restTemplate;

    // Manual constructor injection
    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Processes a payment request by executing an HTTP GET call to a third-party payment provider.
     * Annotated with @CircuitBreaker to apply Resilience4j protection.
     *
     * Annotation Parameters:
     * - name = "paymentService": Links to configuration properties defined under "resilience4j.circuitbreaker.instances.paymentService" in application.yml.
     * - fallbackMethod = "paymentFallback": Specifies the fallback method to execute when failures/timeouts occur or when the circuit is OPEN.
     *
     * @param mode determines target endpoint (success, slow, fail)
     * @return PaymentResponse containing results
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    public PaymentResponse processPayment(String mode) {
        log.info("PaymentService: [Incoming request] processPayment triggered with mode = {}", mode);
        long startTime = System.currentTimeMillis();

        // Dynamically choose target simulation endpoint based on incoming 'mode'
        String url = "http://localhost:8081/thirdparty/success";
        if ("slow".equalsIgnoreCase(mode)) {
            url = "http://localhost:8081/thirdparty/pay";
        } else if ("fail".equalsIgnoreCase(mode)) {
            url = "http://localhost:8081/thirdparty/fail";
        }

        log.info("PaymentService: [Outgoing request] Invoking Third-Party API URL: {}", url);
        
        // Execute request synchronously. Will block for maximum of 2 seconds (read timeout)
        ThirdPartyPaymentResponse thirdPartyResponse = restTemplate.getForObject(url, ThirdPartyPaymentResponse.class);

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("PaymentService: [Third-party response] Received successful API response in {} ms: {}", responseTime, thirdPartyResponse);

        return PaymentResponse.builder()
                .status("SUCCESS")
                .transactionId(thirdPartyResponse != null ? thirdPartyResponse.getTransactionId() : "TXN1000")
                .provider(thirdPartyResponse != null ? thirdPartyResponse.getProvider() : "ThirdPartyAPI")
                .message("Payment processed successfully.")
                .fallback(false)
                .responseTimeMs(responseTime)
                .build();
    }

    /**
     * Fallback method invoked by Resilience4j when:
     * 1. The downstream REST call throws an exception (ResourceAccessException, HttpServerErrorException, etc.).
     * 2. The circuit breaker is in OPEN state (no call is made to processPayment; request fails fast).
     *
     * CRITICAL RULE:
     * The fallback method MUST reside in the same class and have the exact same signature (return type and parameters)
     * as the original method, with one additional trailing parameter of type Throwable.
     *
     * @param mode the input parameter from the original call
     * @param t the throwable exception that triggered fallback execution
     * @return PaymentResponse indicating fallback failure state
     */
    public PaymentResponse paymentFallback(String mode, Throwable t) {
        log.error("PaymentService: [Fallback execution] Circuit Breaker fallback triggered! Exception class: {}, message: {}",
                t.getClass().getName(), t.getMessage());
        
        // Build the requested fallback response structure
        return PaymentResponse.builder()
                .status("FAILED")
                .transactionId("N/A")
                .provider("FallbackProvider")
                .message("Third-party payment service is currently unavailable.")
                .fallback(true)
                .responseTimeMs(0)
                .build();
    }
}
