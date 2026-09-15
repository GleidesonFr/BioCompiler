package br.ufpi.biocompiler.dto;

public record AnalysisStatisticsResponse(
    long correct,
    long invalidBase,
    long startMissing,
    long stopMissing,
    long frameshift,
    long nonsense,
    long dnaCorrect,
    long rnaCorrect,
    long rnaInvalidBase,
    long fivePrimeSite,
    long branchPoint,
    long threePrimeSite,
    long incompleteIntron,
    long alternativeSplicing
) {}
