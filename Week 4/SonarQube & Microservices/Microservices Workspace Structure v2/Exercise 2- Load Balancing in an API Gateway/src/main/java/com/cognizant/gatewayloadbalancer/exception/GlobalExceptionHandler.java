package com.cognizant.gatewayloadbalancer.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Global Exception Handler for the Spring Cloud Gateway.
 * Intercepts all reactive errors in the WebFlux filter chain and translates
 * them into structured JSON payloads with relevant HTTP status codes.
 * annotated with @Order(-1) to take precedence over the default Spring error handler.
 */
@Component
@Order(-1)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles the exception by determining HTTP status and writing a JSON payload.
     *
     * @param exchange the current server exchange
     * @param ex the exception thrown
     * @return Mono<Void> indicating response writing completion
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // If response is already committed, forward the error
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Set response header to JSON
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status;
        String errorCode;
        String errorMessage;

        // Exception mapping logic
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            if (status == HttpStatus.NOT_FOUND) {
                errorCode = "ROUTE_NOT_FOUND";
                errorMessage = "The requested resource/path does not exist on this API Gateway.";
            } else {
                errorCode = "GATEWAY_RESPONSE_ERROR";
                errorMessage = rse.getReason();
            }
        } else if (ex instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            errorCode = "GATEWAY_TIMEOUT";
            errorMessage = "The request to the downstream service timed out (Gateway Timeout).";
        } else if (ex instanceof ConnectException || ex.getMessage().contains("Connection refused")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = "SERVICE_UNAVAILABLE";
            errorMessage = "The target service instance is offline or connection was refused.";
        } else if (ex.getClass().getName().contains("ServiceInstanceNotFoundException") 
                || ex.getMessage().contains("No instances available")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = "NO_SERVICE_INSTANCE";
            errorMessage = "No registered service instances are available to fulfill this request.";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "GATEWAY_INTERNAL_ERROR";
            errorMessage = "An unexpected error occurred within the gateway: " + ex.getMessage();
        }

        response.setStatusCode(status);

        // Log the exception details using SLF4J
        log.error("Gateway Exception Caught: [Code: {}] [Status: {}] [Path: {}] - Message: {}", 
                errorCode, status.value(), exchange.getRequest().getURI().getPath(), ex.getMessage());

        // Construct structured JSON error payload
        Map<String, Object> errorMap = new LinkedHashMap<>();
        errorMap.put("timestamp", LocalDateTime.now().toString());
        errorMap.put("path", exchange.getRequest().getURI().getPath());
        errorMap.put("status", status.value());
        errorMap.put("error", status.getReasonPhrase());
        errorMap.put("code", errorCode);
        errorMap.put("message", errorMessage);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorMap);
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON response: ", e);
            return response.setComplete();
        }
    }
}
