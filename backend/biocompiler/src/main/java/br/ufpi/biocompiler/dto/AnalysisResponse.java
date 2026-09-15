package br.ufpi.biocompiler.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ReadingFrame;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;

public record AnalysisResponse(
    UUID id,
    String originalSequence,
    SequenceType sequenceType,
    ResultType resultType,
    Integer positionStart,
    Integer positionStop,
    ReadingFrame readingFrame,
    String codingRegion,
    String preMrna,
    String matureMrna,
    String message,
    LocalDateTime analysisDate
){
    public static AnalysisResponse from(Analysis analysis) {
        return new AnalysisResponse(
            analysis.getId(),
            analysis.getOriginalSequence(),
            analysis.getSequenceType(),
            analysis.getResultType(),
            analysis.getPositionStart(),
            analysis.getPositionStop(),
            analysis.getReadingFrame(),
            analysis.getCodingRegion(),
            analysis.getPreMrna(),
            analysis.getMatureMrna(),
            analysis.getMessage(),
            analysis.getAnalysisDate()
        );
    }
}

