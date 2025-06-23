package com.hqc.hophuddles.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class DatabaseConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                try {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null || !authentication.isAuthenticated() ||
                            "anonymousUser".equals(authentication.getPrincipal())) {
                        return Optional.of("system");
                    }
                    return Optional.of(authentication.getName());
                } catch (Exception e) {
                    // Fallback to system if any error occurs
                    return Optional.of("system");
                }
            }
        };
    }
}