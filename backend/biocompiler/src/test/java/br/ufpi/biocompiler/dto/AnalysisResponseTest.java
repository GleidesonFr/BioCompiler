package br.ufpi.biocompiler.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;

public class AnalysisResponseTest {
    
    @Test
    void shouldConvertAnalysisToResponse() {

        Analysis analysis = new Analysis();

        analysis.setOriginalSequence("ATGAAACCCTGA");
        analysis.setResultType(ResultType.CORRECT);
        analysis.setPositionStart(0);
        analysis.setPositionStop(9);
        analysis.setCodingRegion("ATGAAACCCTGA");
        analysis.setPreMrna("AUGAAACCCUGA");
        analysis.setMessage("Sequência correta.");

        AnalysisResponse response = AnalysisResponse.from(analysis);

        assertEquals(analysis.getId(), response.id());
        assertEquals("ATGAAACCCTGA", response.originalSequence());
        assertEquals(ResultType.CORRECT, response.resultType());
        assertEquals(0, response.positionStart());
        assertEquals(9, response.positionStop());
        assertEquals("ATGAAACCCTGA", response.codingRegion());
        assertEquals("AUGAAACCCUGA", response.preMrna());
        assertEquals("Sequência correta.", response.message());
    }
}
