package com.kerflowapp.kerflow.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfiguration {

    @Value("${spring.application.name}")
    String applicationName;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(applicationName);
    }

}
