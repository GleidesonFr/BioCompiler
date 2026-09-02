package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.repositories.AnalysisRepository;

@SpringBootTest
public class BioCompilerServiceTest {
    
    @Autowired
    private BioCompilerService bioCompilerService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Test
    void shouldAnalyzeAndSaveCorrectSequence() {
        analysisRepository.deleteAll();

        String sequence = "ATGAAACCCTGA";
        UUID sessionId = UUID.randomUUID();

        Analysis result = bioCompilerService.analyzeAndSave(sequence, sessionId);

        assertNotNull(result);
        assertNotNull(result.getId());

        assertEquals(ResultType.CORRECT, result.getResultType());

        assertEquals("ATGAAACCCTGA", result.getOriginalSequence());

        assertEquals("AUGAAACCCUGA", result.getPreMrna());
    }

    @Test
    void shouldAnalyzeAndSaveMultipleSequences() {

        List<String> sequences = List.of("ATGCCCAAATGA", "ATGAAACCCTGA");

        UUID sessionId = UUID.randomUUID();
        List<Analysis> results = bioCompilerService.analyzeAndSave(sequences, sessionId);

        assertNotNull(results);
        assertEquals(2, results.size());
        assertNotNull(results.get(0).getId());
        assertNotNull(results.get(1).getId());
        assertEquals("ATGCCCAAATGA", results.get(0).getOriginalSequence());
        assertEquals("ATGAAACCCTGA",results.get(1).getOriginalSequence());
    }
}
