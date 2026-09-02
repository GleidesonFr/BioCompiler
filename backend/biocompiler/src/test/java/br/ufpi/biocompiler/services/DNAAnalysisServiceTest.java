package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.Analysis;

public class DNAAnalysisServiceTest {
    private DNAAnalysisService dnaAnalysisService;

    @BeforeEach
    void setUp(){
        DNAValidatorService dnaValidatorService = new DNAValidatorService();
        StartCodonService startCodonService = new StartCodonService();
        StopCodonService stopCodonService = new StopCodonService();
        FrameShiftDetectorService frameShiftDetectorService = new FrameShiftDetectorService();
        NonSenseMutationDetectorService nonSenseMutationDetectorService = new NonSenseMutationDetectorService();
        TranscriptionService transcriptionService = new TranscriptionService();
        AnalysisMessageService analysisMessageService = new AnalysisMessageService();
        
        dnaAnalysisService = new DNAAnalysisService(dnaValidatorService, startCodonService, stopCodonService, transcriptionService, frameShiftDetectorService, nonSenseMutationDetectorService, analysisMessageService);
    }

    @Test
    void shouldIdentityCorrectSequence(){
        String sequence = "ATGAAACCCTGA";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("Sequência correta", result.getMessage());
    }

    @Test
    void shouldIdentifyInvalidBase() {

        String sequence = "ATGAAAXCCTGA";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("BUG - base inválida", result.getMessage());
    }

    @Test
    void shouldIdentifyMissingStartCodon() {

        String sequence = "AAACCCGGGTGA";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("BUG - START ausente", result.getMessage());
    }

    @Test
    void shouldIdentifyMissingStopCodon() {

        String sequence = "ATGAAACCCGGG";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("BUG - STOP ausente", result.getMessage());
    }    

    @Test
    void shouldIdentifyFrameShift() {

        String sequence = "ATGAAAATGA";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("BUG - frameshift", result.getMessage());
    }  

    @Test
    void shouldIdentifyNonsenseMutation() {

        String sequence = "ATGAAATAACCCTGA";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("BUG - nonsense / STOP prematuro", result.getMessage());
    }

    @Test
    void shouldBuildCompleteCorrectAnalysis() {

        String sequence = "ATGAAACCCTGA";

        Analysis result = dnaAnalysisService.analyze(sequence);

        assertEquals("Sequência correta", result.getMessage());

        assertEquals(0, result.getPositionStart());

        assertEquals(9, result.getPositionStop());

        assertEquals("ATGAAACCCTGA", result.getCodingRegion());

        assertEquals("AUGAAACCCUGA", result.getPreMrna());
    }
}
