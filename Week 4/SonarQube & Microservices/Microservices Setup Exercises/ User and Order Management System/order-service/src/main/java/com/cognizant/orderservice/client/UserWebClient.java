package com.cognizant.orderservice.client;

import com.cognizant.orderservice.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class UserWebClient {

    private static final Logger log = LoggerFactory.getLogger(UserWebClient.class);

    private final WebClient webClient;

    // Explicit constructor
    public UserWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public UserDto getUserById(Long id) {
        log.info("WebClient calling User Service for User ID: {}", id);
        return webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    public List<UserDto> getAllUsers() {
        log.info("WebClient calling User Service for All Users");
        return webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {})
                .block();
    }
}
