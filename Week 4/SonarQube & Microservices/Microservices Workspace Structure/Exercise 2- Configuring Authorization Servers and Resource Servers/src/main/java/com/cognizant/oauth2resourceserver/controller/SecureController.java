package com.cognizant.oauth2resourceserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller providing endpoints to demonstrate OAuth2 JWT Resource Server protection.
 */
@RestController
public class SecureController {

    private static final Logger log = LoggerFactory.getLogger(SecureController.class);

    /**
     * Public endpoint accessible without any authentication tokens.
     */
    @GetMapping("/")
    public ResponseEntity<String> getRoot() {
        log.info("Public access: executing root endpoint.");
        return ResponseEntity.ok("OAuth2 Resource Server is running successfully");
    }

    /**
     * Secure endpoint requiring a valid OAuth2 bearer token to access.
     */
    @GetMapping("/secure")
    public ResponseEntity<String> getSecure() {
        log.info("Authorized access: executing secure endpoint.");
        return ResponseEntity.ok("This is a secure endpoint");
    }

    /**
     * Secure profile endpoint that extracts authenticated user details from the JWT.
     * Spring Security automatically injects the resolved Jwt object when annotated with @AuthenticationPrincipal.
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        log.info("Authorized access: retrieving profile for user (subject: {})", jwt.getSubject());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("subject", jwt.getSubject());
        profile.put("issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "Unknown");
        profile.put("audience", jwt.getAudience());
        profile.put("issuedAt", jwt.getIssuedAt() != null ? jwt.getIssuedAt().toString() : "N/A");
        profile.put("expiresAt", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : "N/A");
        profile.put("claims", jwt.getClaims());

        log.debug("Successfully extracted JWT claims: {}", jwt.getClaims());
        return ResponseEntity.ok(profile);
    }
}
