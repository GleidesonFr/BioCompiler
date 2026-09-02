package br.ufpi.biocompiler.services;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.repositories.AnalysisRepository;

@Service
public class AnalysisPersistenceService {
    
    private final AnalysisRepository analysisRepository;

    public AnalysisPersistenceService(AnalysisRepository analysisRepository){
        this.analysisRepository = analysisRepository;
    }

    public Analysis save(Analysis analysis){
        return analysisRepository.save(analysis);
    }

    public Page<Analysis> findAll(Pageable pageable, UUID sessionId){
        return analysisRepository.findAllBySessionIdOrderByAnalysisDateDesc(sessionId, pageable);
    }

    public Analysis findById(UUID id) {
        return analysisRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Análise não encontrada com id: " + id));
    }

    public void deleteAllForSession(UUID sessionId) {
        analysisRepository.deleteAllBySessionId(sessionId);
    }

    public Map<ResultType, Long> getStatistics(UUID sessionId){
        return analysisRepository.countAnalysesByResultType(sessionId)
            .stream()
            .collect(Collectors.toMap(
                row -> (ResultType) row[0],
                row -> (Long) row[1]
            ));
    }
}
