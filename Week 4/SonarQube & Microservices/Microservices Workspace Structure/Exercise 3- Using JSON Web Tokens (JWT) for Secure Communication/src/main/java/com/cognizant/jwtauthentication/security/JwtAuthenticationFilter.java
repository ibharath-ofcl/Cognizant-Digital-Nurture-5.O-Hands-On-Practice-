package com.cognizant.jwtauthentication.security;

import com.cognizant.jwtauthentication.util.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Filter that intercepts incoming HTTP requests to validate JWT tokens.
 * Extends {@link OncePerRequestFilter} to guarantee single execution per request dispatch.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final HandlerExceptionResolver resolver;

    /**
     * Constructor injection. Specifically, {@link HandlerExceptionResolver} is resolved using qualifier
     * to route exceptions in filter chain directly to Global Exception Handler.
     */
    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.tokenProvider = tokenProvider;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        log.debug("Intercepting request for path: '{}'", requestPath);

        // 1. Extract Bearer Token from Authorization Header
        String jwt = getJwtFromRequest(request);

        try {
            // 2. Validate token and populate SecurityContextHolder
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                
                // Store Authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Successfully authenticated user '{}' via JWT, SecurityContext populated.", authentication.getName());
            }
        } catch (JwtException ex) {
            log.error("JWT authentication failed for path: '{}', resolving exception. Error: {}", requestPath, ex.getMessage());
            // Forward exception to HandlerExceptionResolver to trigger GlobalExceptionHandler
            resolver.resolveException(request, response, null, ex);
            return;
        }

        // 3. Continue the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to extract the JWT token from the Authorization header.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            log.debug("Bearer token header found.");
            return bearerToken.substring(7);
        }
        log.debug("No Bearer token found in request headers.");
        return null;
    }
}
