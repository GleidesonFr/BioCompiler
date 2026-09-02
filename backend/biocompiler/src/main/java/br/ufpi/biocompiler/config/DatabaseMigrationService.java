package br.ufpi.biocompiler.config;

import java.util.List;
import java.util.Map;
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
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("PRAGMA table_info('analyses')");
        boolean hasSessionId = columns.stream()
            .anyMatch(row -> "session_id".equalsIgnoreCase(String.valueOf(row.get("name"))));

        if (!hasSessionId) {
            jdbcTemplate.execute("ALTER TABLE analyses ADD COLUMN session_id TEXT");
            jdbcTemplate.update(
                "UPDATE analyses SET session_id = ? WHERE session_id IS NULL OR TRIM(CAST(session_id AS TEXT)) = ''",
                UUID.randomUUID().toString()
            );
        }
    }
}
