package br.ufpi.biocompiler.config;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseMigrationService {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void migrate() {
        Integer columnCount = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = current_schema()
              AND table_name = 'analyses'
              AND column_name = 'session_id'
            """,
            Integer.class
        );
        boolean hasSessionId = columnCount != null && columnCount > 0;

        if (!hasSessionId) {
            jdbcTemplate.execute("ALTER TABLE analyses ADD COLUMN session_id TEXT");
            jdbcTemplate.update(
                "UPDATE analyses SET session_id = ? WHERE session_id IS NULL OR TRIM(CAST(session_id AS TEXT)) = ''",
                UUID.randomUUID().toString()
            );
        }
    }
}
