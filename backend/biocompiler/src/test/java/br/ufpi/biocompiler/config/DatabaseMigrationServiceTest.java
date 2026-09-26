package br.ufpi.biocompiler.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.ResultType;

class DatabaseMigrationServiceTest {

    @Test
    void shouldBuildAtomicConstraintWithEveryResultType() {
        String sql = DatabaseMigrationService.buildResultTypeConstraintSql();

        for (ResultType resultType : ResultType.values()) {
            assertTrue(sql.contains("'" + resultType.name() + "'"));
        }

        assertEquals(1, occurrences(sql, "ALTER TABLE analyses"));
        assertTrue(sql.contains("DROP CONSTRAINT IF EXISTS analyses_result_type_check,"));
        assertTrue(sql.contains("ADD CONSTRAINT analyses_result_type_check"));
    }

    @Test
    void shouldIncludeRibosomeResultTypes() {
        String sql = DatabaseMigrationService.buildResultTypeConstraintSql();

        assertTrue(sql.contains("'CAP_5_ERROR'"));
        assertTrue(sql.contains("'POLY_A_ERROR'"));
        assertTrue(sql.contains("'READING_FRAME_ERROR'"));
    }

    private int occurrences(String value, String search) {
        return value.split(search, -1).length - 1;
    }
}
