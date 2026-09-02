package br.ufpi.biocompiler.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;

@Service
public class BioCompilerService {

    private final DNAAnalysisService dnaAnalysisService;
    private final AnalysisPersistenceService analysisPersistenceService;

    public BioCompilerService(
        DNAAnalysisService dnaAnalysisService,
        AnalysisPersistenceService analysisPersistenceService
    ){
        this.dnaAnalysisService = dnaAnalysisService;
        this.analysisPersistenceService = analysisPersistenceService;
    }
    
    public Analysis analyzeAndSave(String sequence, UUID uuid){
        Analysis analysis = dnaAnalysisService.analyze(sequence);
        analysis.setSessionId(uuid);

        return analysisPersistenceService.save(analysis);
    }

    public Page<Analysis> getHistory(Pageable pageable, UUID sessionId) {
        return analysisPersistenceService.findAll(pageable, sessionId);
    }

    public List<Analysis> analyzeAndSave(List<String> sequences, UUID sessionId){
        return sequences.stream().map(seq -> this.analyzeAndSave(seq, sessionId)).toList();
    }

    public Analysis getAnalysisById(UUID id) {
        return analysisPersistenceService.findById(id);
    }

    public void clearHistory(UUID sessionId) {
        analysisPersistenceService.deleteAllForSession(sessionId);
    }

    public Map<ResultType, Long> getStatistics(UUID sessionId) {
        return analysisPersistenceService.getStatistics(sessionId);
    }
}
