package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.SequenceType;

class SequenceProcessorProxyTest {

    private SequenceProcessorProxy proxy;

    @BeforeEach
    void setUp() {
        AnalysisMessageService messageService = new AnalysisMessageService();
        DNAAnalysisService dnaService = new DNAAnalysisService(
            new DNAValidatorService(),
            new StartCodonService(),
            new StopCodonService(),
            new TranscriptionService(),
            new FrameShiftDetectorService(),
            new NonSenseMutationDetectorService(),
            messageService
        );
        proxy = new SequenceProcessorProxy(
            new SequenceTypeDetector(),
            dnaService,
            new RnaAnalysisService(messageService)
        );
    }

    @Test
    void shouldDelegateDnaAndPreMrnaToTheirProcessors() {
        assertEquals(SequenceType.DNA, proxy.process("ATGAAACCCTGA").getSequenceType());
        assertEquals(SequenceType.PRE_MRNA,
            proxy.process("CCUAUGGCUGUAACCUUUAACUAACAAGAUGGCCUAC").getSequenceType());
    }
}
