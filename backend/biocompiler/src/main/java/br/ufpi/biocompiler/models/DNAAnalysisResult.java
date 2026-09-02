package br.ufpi.biocompiler.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DNAAnalysisResult {
    
    private String originalSequence;
    private ResultType resultType;
    private Integer positionStart;
    private Integer positionStop;
    private ReadingFrame readingFrame;
    private String codingRegion;
    private String preMRNA;
    private String message;
}
