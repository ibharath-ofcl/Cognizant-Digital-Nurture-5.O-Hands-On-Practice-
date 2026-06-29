package com.cognizant.jwtauthentication.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Spring Security 6 Configuration class.
 * Configures stateless JWT authentication, disables CSRF, allows access to public authentication routes,
 * and sets up in-memory users for testing purposes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HandlerExceptionResolver resolver;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.resolver = resolver;
    }

    /**
     * Security filter chain definition.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Spring Security 6 filter chain...");

        http
            // 1. Disable CSRF (Cross-Site Request Forgery) as we are stateless (using JWT)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Configure session management as stateless (no HTTP Session created/used)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. Define authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/authenticate", "/login").permitAll() // permit login/authentication
                .anyRequest().authenticated() // secure all other endpoints
            )
            
            // 4. Configure custom exception handlers for authentication & authorization issues
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    log.error("Unauthorized access attempt to '{}' - Exception: {}", request.getRequestURI(), authException.getMessage());
                    resolver.resolveException(request, response, null, authException);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("Access denied to '{}' - Exception: {}", request.getRequestURI(), accessDeniedException.getMessage());
                    resolver.resolveException(request, response, null, accessDeniedException);
                })
            )
            
            // 5. Add our custom JWT Authentication Filter before the standard UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured successfully.");
        return http.build();
    }

    /**
     * Expose standard AuthenticationManager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Expose BCryptPasswordEncoder bean.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-memory UserDetailsService configuration for testing.
     * Contains two users:
     * - admin/password123 (Roles: ADMIN, USER)
     * - user/user123 (Roles: USER)
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        log.info("Initializing In-Memory user credentials for testing...");
        
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("password123"))
                .roles("ADMIN", "USER")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}
