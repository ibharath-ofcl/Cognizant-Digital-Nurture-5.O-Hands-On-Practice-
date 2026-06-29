package com.cognizant.edgeservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the Edge Service Gateway application.
 * Uses WebTestClient to perform reactive web request assertions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EdgeServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    /**
     * Verifies that the Spring context starts successfully.
     */
    @Test
    void contextLoads() {
        // Assertions are implicit; if the context fails to load, the test fails.
    }

    /**
     * Verifies that the Actuator Health endpoint is active and exposed.
     */
    @Test
    void testActuatorHealth() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    /**
     * Verifies that hitting a non-existent path invokes the GlobalExceptionHandler
     * and returns the expected structured JSON error response.
     */
    @Test
    void testGlobalExceptionHandlerFor404() {
        webTestClient.get().uri("/non-existent-endpoint")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isNotEmpty()
                .jsonPath("$.path").isEqualTo("/non-existent-endpoint")
                .jsonPath("$.trackingId").isNotEmpty();
    }
}
