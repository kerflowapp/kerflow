package com.kerflowapp.kerflow.configuration;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hibernate writes a CHECK constraint listing the enum values when it creates a column, and
 * {@code ddl-auto: update} never rewrites it: a value added to the enum later is rejected by the database.
 * Drops those constraints for the enums that grow; the Java enum stays the only validation.
 * Idempotent, runs after the schema update.
 */
@Component
@RequiredArgsConstructor
public class EnumConstraintsCleanup implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnumConstraintsCleanup.class);

    private static final List<String> CONSTRAINTS = List.of(
        "prospect_messages.prospect_messages_channel_check"
    );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        for (String qualified : CONSTRAINTS) {
            String[] parts = qualified.split("\\.");
            try {
                jdbcTemplate.execute("ALTER TABLE " + parts[0] + " DROP CONSTRAINT IF EXISTS " + parts[1]);
            } catch (RuntimeException e) {
                LOGGER.warn("Could not drop constraint {}", qualified, e);
            }
        }
    }
}
