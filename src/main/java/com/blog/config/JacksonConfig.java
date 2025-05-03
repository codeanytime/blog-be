package com.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for Jackson JSON serialization.
 * Sets up handling for JSON serialization settings.
 */
@Configuration
public class JacksonConfig {

    /**
     * Configures the primary ObjectMapper bean with appropriate modules
     * for handling serialization of entities.
     *
     * @return Configured ObjectMapper instance
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register JavaTimeModule to handle Java 8 date/time types
        objectMapper.registerModule(new JavaTimeModule());

        // Don't fail on empty beans
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Use ISO dates for better compatibility
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }
}