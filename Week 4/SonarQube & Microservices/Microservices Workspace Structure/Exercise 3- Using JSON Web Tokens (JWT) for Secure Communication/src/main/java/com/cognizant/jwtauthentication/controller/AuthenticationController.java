package com.cognizant.jwtauthentication.controller;

import com.cognizant.jwtauthentication.dto.LoginRequest;
import com.cognizant.jwtauthentication.dto.LoginResponse;
import com.cognizant.jwtauthentication.util.JwtTokenProvider;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling authentication requests.
 */
@RestController
@RequestMapping
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Endpoint for user login and token generation.
     * 
     * Flow:
     * 1. Validates user request (checks constraints like @NotBlank).
     * 2. Attempts authentication using AuthenticationManager.
     * 3. If successful, generates a JWT using JwtTokenProvider.
     * 4. Returns the JWT inside LoginResponse.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received authentication request for user: '{}'", loginRequest.getUsername());

        // Perform authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        log.debug("User '{}' authenticated successfully. Proceeding to token generation.", loginRequest.getUsername());

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new LoginResponse(jwt, "Bearer"));
    }
}
