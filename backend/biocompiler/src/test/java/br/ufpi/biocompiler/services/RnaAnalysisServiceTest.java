package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;

class RnaAnalysisServiceTest {

    private RnaAnalysisService rnaAnalysisService;

    @BeforeEach
    void setUp() {
        rnaAnalysisService = new RnaAnalysisService(new AnalysisMessageService());
    }

    @Test
    void shouldGenerateMatureMrnaForValidIntron() {
        Analysis result = rnaAnalysisService.process("CCUAUGGCUGUAACCUUUAACUAACAAGAUGGCCUAC");

        assertEquals(SequenceType.PRE_MRNA, result.getSequenceType());
        assertEquals(ResultType.CORRECT, result.getResultType());
        assertTrue(result.getMatureMrna().startsWith("m7GpppCCUAUGGCUAUGGCCUAC"));
        assertTrue(result.getMatureMrna().endsWith("A".repeat(100)));
    }

    @Test
    void shouldIdentifyEachSpecifiedSplicingError() {
        assertResult("CCUAUGGCUCUAACCUUUAAACCUAACAGGAUGGCCUAC", ResultType.FIVE_PRIME_SITE_ERROR);
        assertResult("CCUAUGGCUGUCCUUUCCUUCCUUCCAGGAUGGCCUAC", ResultType.BRANCH_POINT_ERROR);
        assertResult("CCUAUGGCUGUAACCUUUAACUAACAAUUGGCCUAC", ResultType.THREE_PRIME_SITE_ERROR);
        assertResult("CCUAUGGCUGUAACCUUUAACUAAC", ResultType.INCOMPLETE_INTRON);
        assertResult("CCUAUGGCUGUCCUAACCUAAGGUAACCUAACUAAGGAUGGCCUAC", ResultType.ALTERNATIVE_SPLICING);
    }

    @Test
    void shouldClassifyTheSixtyProfessorCasesEvenly() {
        InputStream input = getClass().getResourceAsStream("/BioCompiler_2_0_entrada_60_casos.txt");
        assertNotNull(input);

        Map<ResultType, Long> counts;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            counts = reader.lines()
                .filter(sequence -> !sequence.isBlank())
                .map(rnaAnalysisService::process)
                .collect(Collectors.groupingBy(
                    Analysis::getResultType,
                    () -> new EnumMap<>(ResultType.class),
                    Collectors.counting()
                ));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

        assertEquals(10L, counts.getOrDefault(ResultType.CORRECT, 0L));
        assertEquals(10L, counts.getOrDefault(ResultType.FIVE_PRIME_SITE_ERROR, 0L));
        assertEquals(10L, counts.getOrDefault(ResultType.BRANCH_POINT_ERROR, 0L));
        assertEquals(10L, counts.getOrDefault(ResultType.THREE_PRIME_SITE_ERROR, 0L));
        assertEquals(10L, counts.getOrDefault(ResultType.INCOMPLETE_INTRON, 0L));
        assertEquals(10L, counts.getOrDefault(ResultType.ALTERNATIVE_SPLICING, 0L));
    }

    private void assertResult(String sequence, ResultType expectedResult) {
        assertEquals(expectedResult, rnaAnalysisService.process(sequence).getResultType());
    }
}
