package com.cognizant.springlearn.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthorizationFilter.class);
    
    // Pad the secret key "secretkey" to 256 bits (32 bytes) to satisfy HS256 requirements and avoid WeakKeyException
    private static final Key SIGNING_KEY;
    static {
        byte[] baseBytes = "secretkey".getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        System.arraycopy(baseBytes, 0, keyBytes, 0, Math.min(baseBytes.length, 32));
        SIGNING_KEY = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    @Autowired
    @org.springframework.context.annotation.Lazy
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        LOGGER.info("START - JWT Authorization Filter");
        String requestURI = request.getRequestURI();
        LOGGER.debug("Request URI: {}", requestURI);

        // Skip JWT validation for /authenticate endpoint (as it uses HTTP Basic)
        if ("/authenticate".equals(requestURI)) {
            LOGGER.info("Skipping JWT filter for /authenticate endpoint");
            filterChain.doFilter(request, response);
            LOGGER.info("END - JWT Authorization Filter");
            return;
        }

        String authHeader = request.getHeader("Authorization");
        LOGGER.debug("Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Missing or invalid Bearer token prefix. Passing to filter chain.");
            filterChain.doFilter(request, response);
            LOGGER.info("END - JWT Authorization Filter");
            return;
        }

        String jwtToken = authHeader.substring(7);
        LOGGER.debug("Extracted JWT Token: {}", jwtToken);

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SIGNING_KEY)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String username = claims.getSubject();
            LOGGER.info("Successfully validated JWT token for user: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                LOGGER.info("Set SecurityContext for user: {}", username);
            }
        } catch (ExpiredJwtException e) {
            LOGGER.error("JWT Token has expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"JWT Token has expired\"}");
            LOGGER.info("END - JWT Authorization Filter (Expired)");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            LOGGER.error("Invalid JWT Token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid JWT Token\"}");
            LOGGER.info("END - JWT Authorization Filter (Invalid)");
            return;
        }

        filterChain.doFilter(request, response);
        LOGGER.info("END - JWT Authorization Filter");
    }
}
