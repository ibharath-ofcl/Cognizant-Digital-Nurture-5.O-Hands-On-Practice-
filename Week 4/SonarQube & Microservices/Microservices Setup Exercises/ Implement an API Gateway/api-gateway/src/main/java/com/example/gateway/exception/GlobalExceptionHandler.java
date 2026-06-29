package com.example.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.TimeoutException;

/**
 * Global reactive Exception Handler for API Gateway.
 * Intercepts routing failures, downstream service disconnects, gateway timeouts,
 * and handles formatting them as JSON responses.
 */
@Component
@Order(-2) // Sets priority higher than the default Spring Boot ErrorWebExceptionHandler
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // If the response is already committed, forward the error downstream
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Set response header to application/json
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        // Classify standard exceptions to mapping HTTP Statuses
        if (ex instanceof ResponseStatusException) {
            HttpStatusCode statusCode = ((ResponseStatusException) ex).getStatusCode();
            status = HttpStatus.resolve(statusCode.value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else if (ex instanceof TimeoutException) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Gateway Timeout: The downstream service timed out.";
        } else if (ex instanceof java.net.ConnectException || ex.getClass().getName().contains("ConnectException")) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service Unavailable: Downstream service is offline or unreachable.";
        }

        response.setStatusCode(status);

        log.error("Gateway intercepted exception on request '{}': Status = {} | Reason = {}", 
                exchange.getRequest().getPath(), status.value(), message, ex);

        // Populate error payload
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value()
        );

        try {
            // Write JSON response body
            byte[] responseBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(responseBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Failed to map Exception Response as JSON structure", e);
            return Mono.error(e);
        }
    }
}
