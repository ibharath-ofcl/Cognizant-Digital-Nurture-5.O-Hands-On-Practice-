package com.cognizant.jwtauthentication.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller showcasing secured endpoints protected by JWT authentication.
 */
@RestController
@RequestMapping
public class SecureController {

    private static final Logger log = LoggerFactory.getLogger(SecureController.class);

    /**
     * Endpoint showing a basic protected string response.
     */
    @GetMapping("/secure")
    public ResponseEntity<String> getSecureData() {
        log.info("Accessing /secure endpoint...");
        return ResponseEntity.ok("This is a secure endpoint.");
    }

    /**
     * Endpoint returning the profile details of the authenticated user.
     * 
     * How the authenticated user is retrieved:
     * We can inject {@link UserDetails} using the {@code @AuthenticationPrincipal} annotation.
     * Behind the scenes, Spring Security looks up the {@link Authentication} object stored in the 
     * {@link SecurityContextHolder}'s context by our {@code JwtAuthenticationFilter}, 
     * extracts its principal, and resolves it.
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Accessing /profile endpoint for user: '{}'...", userDetails.getUsername());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", userDetails.getUsername());
        profile.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        profile.put("accountNonExpired", userDetails.isAccountNonExpired());
        profile.put("accountNonLocked", userDetails.isAccountNonLocked());
        profile.put("credentialsNonExpired", userDetails.isCredentialsNonExpired());
        profile.put("enabled", userDetails.isEnabled());

        return ResponseEntity.ok(profile);
    }

    /**
     * Root endpoint to check API status.
     */
    @GetMapping("/")
    public ResponseEntity<String> getRootStatus() {
        log.info("Accessing root status endpoint...");
        return ResponseEntity.ok("JWT Authentication is working successfully.");
    }
}
