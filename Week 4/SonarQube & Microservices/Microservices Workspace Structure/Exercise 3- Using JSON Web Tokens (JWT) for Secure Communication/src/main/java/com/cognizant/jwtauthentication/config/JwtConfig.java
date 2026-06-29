package com.cognizant.jwtauthentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration class to map application.yml properties prefix 'jwt'.
 * 
 * In Spring Boot 3, using {@code @ConfigurationProperties} is highly preferred over {@code @Value} because:
 * 1. **Type Safety**: Maps properties into typed fields automatically.
 * 2. **JSR-303 Validation**: Allows validation annotations like {@code @NotBlank} and {@code @Min}.
 * 3. **Relaxed Binding**: Handles variations of names (e.g., camelCase, kebab-case) smoothly.
 * 4. **Encapsulation & Reusability**: Centralizes security parameters rather than repeating {@code @Value} across multiple beans.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtConfig {

    @NotBlank(message = "JWT Secret Key must not be blank")
    private String secret;

    @NotNull(message = "JWT Expiration Time must not be null")
    @Min(value = 1000, message = "JWT Expiration Time must be at least 1000 milliseconds")
    private Long expiration;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }
}
