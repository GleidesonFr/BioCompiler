package br.ufpi.biocompiler.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.StartCodon;
import br.ufpi.biocompiler.models.StopCodon;

@Service
public class DNAAnalysisService {
    
    private final DNAValidatorService dnaValidatorService;
    private final StartCodonService startCodonService;
    private final StopCodonService stopCodonService;
    private final TranscriptionService transcriptionService;
    private final FrameShiftDetectorService frameShiftDetectorService;
    private final NonSenseMutationDetectorService nonSenseMutationDetectorService;
    private final AnalysisMessageService analysisMessageService;

    public DNAAnalysisService(
        DNAValidatorService dnaValidatorService,
        StartCodonService startCodonService,
        StopCodonService stopCodonService,
        TranscriptionService transcriptionService,
        FrameShiftDetectorService frameShiftDetectorService,
        NonSenseMutationDetectorService nonSenseMutationDetectorService,
        AnalysisMessageService analysisMessageService){

        this.dnaValidatorService = dnaValidatorService;
        this.startCodonService = startCodonService;
        this.stopCodonService = stopCodonService;
        this.transcriptionService = transcriptionService;
        this.frameShiftDetectorService = frameShiftDetectorService;
        this.nonSenseMutationDetectorService = nonSenseMutationDetectorService;
        this.analysisMessageService = analysisMessageService;
    }

    public Analysis analyze(String sequence){
        Analysis analysis = new Analysis();
        analysis.setOriginalSequence(sequence);
        analysis.setAnalysisDate(LocalDateTime.now());

        if(!dnaValidatorService.isValidDNA(sequence)){
            analysis.setResultType(ResultType.INVALID_BASE);
            return finishAnalysis(analysis, ResultType.INVALID_BASE);
        }

        StartCodon startCodon = startCodonService.findStartCodon(sequence)
        .orElse(null);

        if(startCodon == null){
            analysis.setResultType(ResultType.START_CODON_NOT_FOUND);
            return finishAnalysis(analysis, ResultType.START_CODON_NOT_FOUND);
        }

        analysis.setPositionStart(startCodon.getPosition());
        analysis.setReadingFrame(startCodon.getReadingFrame());

        if(frameShiftDetectorService.isFrameShift(sequence, startCodon.getPosition())){
            analysis.setResultType(ResultType.FRAME_SHIFT);
            return finishAnalysis(analysis, ResultType.FRAME_SHIFT);
        }

        List<StopCodon> stopCodons = stopCodonService.findStopCodon(sequence, analysis.getPositionStart());
        
        if(stopCodons.isEmpty()){
            analysis.setResultType(ResultType.STOP_CODON_NOT_FOUND);
            return finishAnalysis(analysis, ResultType.STOP_CODON_NOT_FOUND);
        }

        if(nonSenseMutationDetectorService.isNonSenseMutation(stopCodons, startCodon.getReadingFrame())){
            analysis.setResultType(ResultType.NONSENSE_MUTATION);
            return finishAnalysis(analysis, ResultType.NONSENSE_MUTATION);
        }

        StopCodon stopCodon = stopCodons.get(0);
        analysis.setPositionStop(stopCodon.getPosition());

        String codingRegion = sequence.substring(startCodon.getPosition(), stopCodon.getPosition() + 3);
        analysis.setCodingRegion(codingRegion);

        String preMrna = transcriptionService.transcribe(codingRegion);
        analysis.setPreMrna(preMrna);

        analysis.setResultType(ResultType.CORRECT);
        return finishAnalysis(analysis, ResultType.CORRECT);
        
    }

    private Analysis finishAnalysis(Analysis analysis, ResultType resultType){
        analysis.setMessage(analysisMessageService.generateMessage(resultType));

        return analysis;
    }
}
