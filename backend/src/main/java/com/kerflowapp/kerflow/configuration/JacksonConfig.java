package com.kerflowapp.kerflow.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

@Configuration
public class JacksonConfig {

    /**
     * Customises the JsonMapper Spring Boot auto-configures instead of defining a mapper
     * of our own: since Jackson 3 the mapper is immutable and built from a builder, and
     * this way the web layer and every injected mapper share the same configuration.
     */
    @Bean
    public JsonMapperBuilderCustomizer kerflowJsonMapperBuilderCustomizer() {
        return builder -> builder
            .changeDefaultPropertyInclusion(value -> value.withValueInclusion(JsonInclude.Include.NON_NULL))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
