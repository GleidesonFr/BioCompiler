package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.repositories.AnalysisRepository;

@SpringBootTest
public class AnalysisPersistenceServiceTest {
    
    @Autowired
    private AnalysisPersistenceService persistenceService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Test
    void shouldSaveAnalysis(){
        analysisRepository.deleteAll();
        Analysis analysis = new Analysis();

        analysis.setOriginalSequence("ATGAAACCCTGA");
        analysis.setResultType(ResultType.CORRECT);

        Analysis saved = persistenceService.save(analysis);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertEquals("ATGAAACCCTGA", saved.getOriginalSequence());
        assertEquals(ResultType.CORRECT, saved.getResultType());
    }

    @Test
    void shouldFindAnalysesOrderedByDateDescending() {
        analysisRepository.deleteAll();

        UUID sessionId = UUID.randomUUID();

        Analysis first = new Analysis();
        first.setOriginalSequence("ATGAAACCCTGA");
        first.setResultType(ResultType.CORRECT);
        first.setSessionId(sessionId);

        persistenceService.save(first);

        Analysis second = new Analysis();
        second.setOriginalSequence("ATGAAA");
        second.setResultType(ResultType.STOP_CODON_NOT_FOUND);
        second.setSessionId(sessionId);

        persistenceService.save(second);

        Page<Analysis> analyses = persistenceService.findAll(Pageable.unpaged(), sessionId);

        assertNotNull(analyses);
        assertEquals(2, analyses.getNumberOfElements());
    }
}
