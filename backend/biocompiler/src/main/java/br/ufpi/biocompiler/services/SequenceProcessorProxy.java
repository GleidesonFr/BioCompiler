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

    public SequenceProcessorProxy(
        SequenceTypeDetector sequenceTypeDetector,
        DNAAnalysisService dnaAnalysisService,
        RnaAnalysisService rnaAnalysisService
    ) {
        this.sequenceTypeDetector = sequenceTypeDetector;
        this.dnaAnalysisService = dnaAnalysisService;
        this.rnaAnalysisService = rnaAnalysisService;
    }

    @Override
    public Analysis process(String sequence) {
        SequenceType sequenceType = sequenceTypeDetector.detect(sequence);
        return switch (sequenceType) {
            case DNA -> dnaAnalysisService.process(sequence);
            case PRE_MRNA -> rnaAnalysisService.process(sequence);
        };
    }
}
