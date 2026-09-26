package br.ufpi.biocompiler.config;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.ufpi.biocompiler.models.ResultType;
import jakarta.annotation.PostConstruct;

@Component
@Profile("!terminal")
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

        updateResultTypeConstraint();
    }

    private void updateResultTypeConstraint() {
        jdbcTemplate.execute(buildResultTypeConstraintSql());
    }

    static String buildResultTypeConstraintSql() {
        String allowedValues = Arrays.stream(ResultType.values())
            .map(resultType -> "'" + resultType.name() + "'")
            .collect(Collectors.joining(", "));

        return """
            ALTER TABLE analyses
            DROP CONSTRAINT IF EXISTS analyses_result_type_check,
            ADD CONSTRAINT analyses_result_type_check CHECK (result_type IN (%s))
            """.formatted(allowedValues);
    }
}
