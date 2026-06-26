package com.example.employeemanagementsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // Return a dummy auditor name for the purpose of this exercise.
        // In a production application with Spring Security, this would fetch from the SecurityContextHolder.
        return () -> Optional.of("Admin");
    }
}
