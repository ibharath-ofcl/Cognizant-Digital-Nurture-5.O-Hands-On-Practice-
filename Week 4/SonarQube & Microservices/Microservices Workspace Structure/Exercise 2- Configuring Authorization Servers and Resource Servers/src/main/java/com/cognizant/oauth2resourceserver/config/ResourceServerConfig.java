package com.cognizant.oauth2resourceserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Security configuration class for the Resource Server.
 * Configured using modern Spring Security 6 filter chain configuration.
 */
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    private static final Logger log = LoggerFactory.getLogger(ResourceServerConfig.class);
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection for ObjectMapper (best practice).
     */
    public ResourceServerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Configures the main HTTP security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Resource Server SecurityFilterChain Configuration...");

        http
            // 1. Disable CSRF since APIs are stateless and use bearer tokens
            .csrf(csrf -> csrf.disable())

            // 2. Configure stateless session management (no session creation in servlet container)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Configure request authorization policies
            .authorizeHttpRequests(auth -> auth
                // Allow anonymous access to root endpoint for health checks
                .requestMatchers("/").permitAll()
                // All other endpoints require a validated JWT token
                .anyRequest().authenticated()
            )

            // 4. Configure OAuth2 Resource Server with JWT decoding support
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
                // Custom authentication entry point to handle invalid or missing tokens in filter chain
                .authenticationEntryPoint(authenticationEntryPoint())
                // Custom access denied handler to handle insufficient scope/privileges
                .accessDeniedHandler(accessDeniedHandler())
            )

            // 5. General Exception handling config for the filter chain
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            );

        log.info("Resource Server SecurityFilterChain configured successfully.");
        return http.build();
    }

    /**
     * Custom entrypoint to return structured JSON when authentication fails.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.error("Authentication failed for request to {}: {}", request.getRequestURI(), authException.getMessage());
            
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", authException.getMessage() != null ? authException.getMessage() : "Authentication token is missing or invalid");
            errorDetails.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getWriter(), errorDetails);
        };
    }

    /**
     * Custom access denied handler to return structured JSON when authorization fails.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.error("Access denied for request to {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
            
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("timestamp", LocalDateTime.now().toString());
            errorDetails.put("status", HttpStatus.FORBIDDEN.value());
            errorDetails.put("error", "Forbidden");
            errorDetails.put("message", "Access denied. Insufficient permissions/scopes to access this resource.");
            errorDetails.put("path", request.getRequestURI());

            objectMapper.writeValue(response.getWriter(), errorDetails);
        };
    }
}
