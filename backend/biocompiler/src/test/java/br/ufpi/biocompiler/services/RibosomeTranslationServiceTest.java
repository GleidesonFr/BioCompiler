package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;

class RibosomeTranslationServiceTest {

    private RibosomeTranslationService ribosomeTranslationService;
    private static final String POLY_A_100 = "A".repeat(100);

    @BeforeEach
    void setUp() {
        ribosomeTranslationService = new RibosomeTranslationService(new AnalysisMessageService());
    }

    @Test
    void shouldTranslateCanonicalCorrectSequence() {
        // Exemplo 14.1 da especificação:
        // mRNA: AUG GCU AAA CCG UAA
        // Proteína: Met-Ala-Lys-Pro
        String sequence = "m7GpppCCAUGGCUAAACCGUAAGG" + POLY_A_100;

        Analysis result = ribosomeTranslationService.process(sequence);

        assertEquals(SequenceType.MATURE_MRNA, result.getSequenceType());
        assertEquals(ResultType.CORRECT, result.getResultType());
        assertEquals("Met-Ala-Lys-Pro", result.getProtein());
        assertEquals("AUGGCUAAACCGUAA", result.getCodingRegion());
        assertEquals(2, result.getPositionStart());
        assertEquals(14, result.getPositionStop());
    }

    @Test
    void shouldIdentifyCap5ErrorWhenPrefixIsMissingOrIncorrect() {
        // Exemplo 14.2 da especificação:
        String sequence = "CCAUGGCUAAACCGUAAGG" + POLY_A_100;

        Analysis result = ribosomeTranslationService.process(sequence);

        assertEquals(ResultType.CAP_5_ERROR, result.getResultType());
        assertNull(result.getProtein());
        assertEquals("BUG - CAP 5'", result.getMessage());
    }

    @Test
    void shouldIdentifyMissingStartCodon() {
        // Exemplo 14.3 da especificação:
        String sequence = "m7GpppCCGCCGCUAAACCGUAAGG" + POLY_A_100;

        Analysis result = ribosomeTranslationService.process(sequence);

        assertEquals(ResultType.START_CODON_NOT_FOUND, result.getResultType());
        assertNull(result.getProtein());
        assertEquals("BUG - START ausente", result.getMessage());
    }

    @Test
    void shouldIdentifyMissingStopCodon() {
        // Exemplo 14.4 da especificação:
        String sequence = "m7GpppCCAUGGCUAAACCGGGCGG" + POLY_A_100;

        Analysis result = ribosomeTranslationService.process(sequence);

        assertEquals(ResultType.STOP_CODON_NOT_FOUND, result.getResultType());
        assertNull(result.getProtein());
        assertEquals("BUG - STOP ausente", result.getMessage());
    }

    @Test
    void shouldIdentifyReadingFrameError() {
        // Exemplo 14.5 da especificação:
        // Inserção de uma base após GCU: AUG GCU AAAA CCG UAA GG
        String sequence = "m7GpppCCAUGGCUAAAACCGUAAGG" + POLY_A_100;

        Analysis result = ribosomeTranslationService.process(sequence);

        assertEquals(ResultType.READING_FRAME_ERROR, result.getResultType());
        assertNull(result.getProtein());
        assertEquals("BUG - quadro de leitura", result.getMessage());
    }

    @Test
    void shouldIdentifyPolyATailErrorWhenFewerOrMoreThan100Adenines() {
        // Exemplo 14.6 da especificação (80 adeninas):
        String sequence80 = "m7GpppCCAUGGCUAAACCGUAAGG" + "A".repeat(80);
        Analysis result80 = ribosomeTranslationService.process(sequence80);
        assertEquals(ResultType.POLY_A_ERROR, result80.getResultType());
        assertEquals("BUG - cauda poli-A", result80.getMessage());
        assertNull(result80.getProtein());

        // Cauda alterada (101 adeninas):
        String sequence101 = "m7GpppCCAUGGCUAAACCGUAAGG" + "A".repeat(101);
        Analysis result101 = ribosomeTranslationService.process(sequence101);
        assertEquals(ResultType.POLY_A_ERROR, result101.getResultType());
    }

    @Test
    void shouldTranslateSequencesWithOtherStopCodons() {
        // Teste com UAG:
        String seqUag = "m7GpppGGAUGUUUUACUAGCC" + POLY_A_100;
        Analysis resUag = ribosomeTranslationService.process(seqUag);
        assertEquals(ResultType.CORRECT, resUag.getResultType());
        assertEquals("Met-Phe-Tyr", resUag.getProtein());

        // Teste com UGA:
        String seqUga = "m7GpppGGAUGGAGCAGUGACC" + POLY_A_100;
        Analysis resUga = ribosomeTranslationService.process(seqUga);
        assertEquals(ResultType.CORRECT, resUga.getResultType());
        assertEquals("Met-Glu-Gln", resUga.getProtein());
    }

    @Test
    void shouldIdentifyInvalidBaseInRna() {
        String invalidSeq = "m7GpppCCAUGGCUAAACCGUAAGGX" + POLY_A_100;
        Analysis result = ribosomeTranslationService.process(invalidSeq);
        assertEquals(ResultType.INVALID_BASE, result.getResultType());
    }
}
