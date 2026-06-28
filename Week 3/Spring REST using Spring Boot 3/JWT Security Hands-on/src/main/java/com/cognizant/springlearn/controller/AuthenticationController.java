package com.cognizant.springlearn.controller;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationController.class);

    // Pad the secret key "secretkey" to 256 bits (32 bytes) to satisfy HS256 requirements and avoid WeakKeyException
    private static final Key SIGNING_KEY;
    static {
        byte[] baseBytes = "secretkey".getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        System.arraycopy(baseBytes, 0, keyBytes, 0, Math.min(baseBytes.length, 32));
        SIGNING_KEY = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    @GetMapping("/authenticate")
    public Map<String, String> authenticate(@RequestHeader("Authorization") String authHeader) {
        LOGGER.info("START - authenticate endpoint");
        LOGGER.debug("Received Authorization Header: {}", authHeader);

        String username = getUser(authHeader);
        String jwtToken = generateJwt(username);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);

        LOGGER.debug("Authentication successful. Generated token for user: {}", username);
        LOGGER.info("END - authenticate endpoint");
        return response;
    }

    private String getUser(String authHeader) {
        LOGGER.info("START - getUser from Authorization header");
        LOGGER.debug("Decoding authHeader: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            LOGGER.error("Invalid Basic Authentication header format");
            throw new IllegalArgumentException("Invalid Basic Authentication header");
        }

        try {
            // Remove "Basic " prefix
            String base64Token = authHeader.substring(6).trim();
            
            // Decode Base64 bytes
            byte[] decodedBytes = Base64.getDecoder().decode(base64Token);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // Extract username (credentials are in format 'username:password')
            int colonIndex = credentials.indexOf(":");
            if (colonIndex == -1) {
                LOGGER.error("Invalid credentials format in decoded basic header (missing colon)");
                throw new IllegalArgumentException("Invalid credentials format");
            }

            String username = credentials.substring(0, colonIndex);
            LOGGER.debug("Decoded credentials: '{}'. Extracted username: '{}'", credentials, username);
            LOGGER.info("END - getUser");
            return username;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Exception during Base64 decoding of authHeader: {}", e.getMessage());
            throw e;
        }
    }

    private String generateJwt(String username) {
        LOGGER.info("START - generateJwt for user: {}", username);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        // Expiry = 20 minutes (20 * 60 * 1000 milliseconds)
        long expMillis = nowMillis + (20 * 60 * 1000);
        Date exp = new Date(expMillis);

        // Generate JWT Token using HS256 and secretkey
        String jwtToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
                .compact();

        LOGGER.debug("JWT Token generated: {}", jwtToken);
        LOGGER.info("END - generateJwt");
        return jwtToken;
    }
}
