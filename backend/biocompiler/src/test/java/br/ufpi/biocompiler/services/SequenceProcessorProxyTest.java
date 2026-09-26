package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
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
            new RnaAnalysisService(messageService),
            new RibosomeTranslationService(messageService)
        );
    }

    @Test
    void shouldDelegateDnaAndPreMrnaToTheirProcessors() {
        assertEquals(SequenceType.DNA, proxy.process("ATGAAACCCTGA").getSequenceType());
        assertEquals(SequenceType.PRE_MRNA,
            proxy.process("CCUAUGGCUGUAACCUUUAACUAACAAGAUGGCCUAC").getSequenceType());
        assertEquals(SequenceType.MATURE_MRNA,
            proxy.process("m7GpppCCAUGGCUAAACCGUAAGG" + "A".repeat(100)).getSequenceType());
    }

    @Test
    void shouldDetectMultilinePolyATailAndPreserveOriginalSequence() {
        String sequence = "CCAUGGCUAAACCGUAAGG" + "A".repeat(95) + "\n\t " + "A".repeat(5);

        Analysis result = proxy.process(sequence);

        assertEquals(SequenceType.MATURE_MRNA, result.getSequenceType());
        assertEquals(ResultType.CAP_5_ERROR, result.getResultType());
        assertEquals(sequence, result.getOriginalSequence());
    }

    @Test
    void shouldTranslateCappedMatureMrnaWithWhitespaceInsidePolyATail() {
        String sequence = "m7GpppCCAUGGCUAAACCGUAAGG"
            + "A".repeat(40) + " \r\n\t" + "A".repeat(60);

        Analysis result = proxy.process(sequence);

        assertEquals(SequenceType.MATURE_MRNA, result.getSequenceType());
        assertEquals(ResultType.CORRECT, result.getResultType());
        assertEquals("Met-Ala-Lys-Pro", result.getProtein());
        assertEquals(sequence, result.getOriginalSequence());
    }

    @Test
    void shouldValidateRequiredMatureMrnaMarkersAfterDetection() {
        Analysis missingPolyA = proxy.process("m7GpppCCAUGGCUAAACCGUAAGG" + "A".repeat(80));
        Analysis missingCap = proxy.process("CCAUGGCUAAACCGUAAGG" + "A".repeat(100));

        assertEquals(SequenceType.MATURE_MRNA, missingPolyA.getSequenceType());
        assertEquals(ResultType.POLY_A_ERROR, missingPolyA.getResultType());
        assertEquals(SequenceType.MATURE_MRNA, missingCap.getSequenceType());
        assertEquals(ResultType.CAP_5_ERROR, missingCap.getResultType());
    }

    @Test
    void shouldNotRemoveInvalidBasesDuringNormalization() {
        String sequence = "m7GpppCCAUGGCUAAACCGUAAGGX" + "A".repeat(100);

        Analysis result = proxy.process(sequence);

        assertEquals(SequenceType.MATURE_MRNA, result.getSequenceType());
        assertEquals(ResultType.INVALID_BASE, result.getResultType());
    }
}
