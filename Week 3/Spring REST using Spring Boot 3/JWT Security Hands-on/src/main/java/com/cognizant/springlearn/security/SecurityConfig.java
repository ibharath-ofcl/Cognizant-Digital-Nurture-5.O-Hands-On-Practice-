package com.cognizant.springlearn.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    @org.springframework.context.annotation.Lazy
    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        LOGGER.info("START - passwordEncoder bean creation");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        LOGGER.info("END - passwordEncoder bean creation");
        return encoder;
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        LOGGER.info("START - userDetailsService bean creation");
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("pwd"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("pwd"))
                .roles("ADMIN")
                .build();

        LOGGER.info("END - userDetailsService bean creation");
        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        LOGGER.info("START - authenticationManager bean creation");
        AuthenticationManager authManager = authenticationConfiguration.getAuthenticationManager();
        LOGGER.info("END - authenticationManager bean creation");
        return authManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.info("START - securityFilterChain configuration");
        http
            .csrf(csrf -> {
                LOGGER.debug("Disabling CSRF protection");
                csrf.disable();
            })
            .sessionManagement(session -> {
                LOGGER.debug("Configuring stateless session policy");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(auth -> {
                LOGGER.debug("Configuring authorization request matchers");
                auth.requestMatchers("/authenticate").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/error").permitAll()
                    .requestMatchers("/countries").hasRole("USER")
                    .anyRequest().authenticated();
            })
            .httpBasic(Customizer.withDefaults())
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        
        LOGGER.info("END - securityFilterChain configuration");
        return http.build();
    }
}
