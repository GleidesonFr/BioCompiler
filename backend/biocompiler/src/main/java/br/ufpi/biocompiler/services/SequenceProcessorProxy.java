package br.ufpi.biocompiler.services;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.SequenceType;
import br.ufpi.biocompiler.utils.SequenceProcessor;

@Service
public class SequenceProcessorProxy implements SequenceProcessor {
    
    private final SequenceTypeDetector sequenceTypeDetector;
    private final DNAAnalysisService dnaAnalysisService;
    private final RnaAnalysisService rnaAnalysisService;
    private final RibosomeTranslationService ribosomeTranslationService;

    public SequenceProcessorProxy(
        SequenceTypeDetector sequenceTypeDetector,
        DNAAnalysisService dnaAnalysisService,
        RnaAnalysisService rnaAnalysisService,
        RibosomeTranslationService ribosomeTranslationService
    ) {
        this.sequenceTypeDetector = sequenceTypeDetector;
        this.dnaAnalysisService = dnaAnalysisService;
        this.rnaAnalysisService = rnaAnalysisService;
        this.ribosomeTranslationService = ribosomeTranslationService;
    }

    @Override
    public Analysis process(String sequence) {
        String normalizedSequence = sequenceTypeDetector.normalize(sequence);
        SequenceType sequenceType = sequenceTypeDetector.detect(normalizedSequence);
        Analysis analysis = switch (sequenceType) {
            case DNA -> dnaAnalysisService.process(normalizedSequence);
            case PRE_MRNA -> rnaAnalysisService.process(normalizedSequence);
            case MATURE_MRNA -> ribosomeTranslationService.process(normalizedSequence);
        };
        analysis.setOriginalSequence(sequence);
        return analysis;
    }
}
