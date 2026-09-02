package br.ufpi.biocompiler.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public record AnalysisRequest(@NotEmpty String sequence, UUID sessionId){

    public static AnalysisRequest from(String sequence, UUID sessionId) {
        return new AnalysisRequest(sequence, sessionId);
    }
}
