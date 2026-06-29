package com.cognizant.centralizedauthentication.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/")
    public String welcome() {
        log.info("Accessing landing page. Displaying welcome message.");
        return "Welcome to Centralized Authentication using OAuth2";
    }

    @GetMapping("/user")
    public Map<String, Object> getUserDetails(Principal principal) {
        log.info("Accessing authenticated user details endpoint.");
        
        if (principal == null) {
            log.error("Principal is null. Request is unauthorized.");
            throw new IllegalStateException("User is not authenticated");
        }
        
        log.debug("Authentication class type: {}", principal.getClass().getName());

        if (principal instanceof OAuth2AuthenticationToken authToken) {
            OAuth2User oauth2User = authToken.getPrincipal();
            log.info("User successfully authenticated via provider: {}", authToken.getAuthorizedClientRegistrationId());
            log.debug("Extracted user attributes from OAuth2 token: {}", oauth2User.getAttributes());
            return oauth2User.getAttributes();
        }
        
        log.warn("Principal is not an instance of OAuth2AuthenticationToken. Returning fallback principal name.");
        return Map.of("name", principal.getName());
    }
}
