package br.ufpi.biocompiler.dto;

public record AnalysisStatisticsResponse(
    long correct,
    long invalidBase,
    long startMissing,
    long stopMissing,
    long frameshift,
    long nonsense
) {}
