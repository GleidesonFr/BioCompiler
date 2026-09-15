package br.ufpi.biocompiler.models;

import lombok.Getter;

@Getter
public enum ResultType {
    CORRECT("Correto"),
    INVALID_BASE("Base Inválida"),
    START_CODON_NOT_FOUND("Códon de Início Não Encontrado"),
    STOP_CODON_NOT_FOUND("Códon de Parada Não Encontrado"),
    FRAME_SHIFT("Desvio de Quadro"),
    NONSENSE_MUTATION("Mutação sem Sentido"),
    FIVE_PRIME_SITE_ERROR("BUG - sítio 5'"),
    BRANCH_POINT_ERROR("BUG - branch point"),
    THREE_PRIME_SITE_ERROR("BUG - sítio 3'"),
    INCOMPLETE_INTRON("BUG - íntron incompleto"),
    ALTERNATIVE_SPLICING("AMBÍGUO - splicing alternativo");

    private final String description;

    ResultType(String description) {
        this.description = description;
    }
}
