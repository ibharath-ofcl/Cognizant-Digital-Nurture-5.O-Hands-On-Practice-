package com.cognizant.centralizedauthentication.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing SecurityFilterChain configured for Spring Boot 3 and Spring Security 6.");

        http
            // Configure Request Authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll() // Permit access to the welcome page
                .anyRequest().authenticated()      // Authenticate all other requests
            )
            // Configure OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                // When using default login page, Spring Security auto-generates a page at "/login" 
                // containing a link to initiate Google Login: "/oauth2/authorization/google"
                .defaultSuccessUrl("/user", true)
            )
            // Configure Logout
            .logout(logout -> logout
                .logoutSuccessUrl("/")             // Redirect to welcome page after successful logout
                .invalidateHttpSession(true)       // Invalidate HTTP session to clear security context
                .clearAuthentication(true)         // Clear authentication details
                .deleteCookies("JSESSIONID")       // Delete JSESSIONID cookie
                .permitAll()
            )
            // Configure CSRF protection (Enabled by default, explicitly configuring it)
            .csrf(csrf -> csrf
                // CSRF is enabled by default to protect state-changing POST/PUT requests.
                // We keep it enabled but configure it explicitly.
                .ignoringRequestMatchers("/")
            )
            // Session Management
            .sessionManagement(session -> session
                // Session creation policy: IF_REQUIRED (Spring Security will create a session when needed)
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)                // Restrict to 1 active session per user
            );

        log.debug("SecurityFilterChain bean initialized successfully.");
        return http.build();
    }
}
