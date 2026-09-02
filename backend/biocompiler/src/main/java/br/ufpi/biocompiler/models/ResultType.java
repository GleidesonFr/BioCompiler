package br.ufpi.biocompiler.models;

import lombok.Getter;

@Getter
public enum ResultType {
    CORRECT("Correto"),
    INVALID_BASE("Base Inválida"),
    START_CODON_NOT_FOUND("Códon de Início Não Encontrado"),
    STOP_CODON_NOT_FOUND("Códon de Parada Não Encontrado"),
    FRAME_SHIFT("Desvio de Quadro"),
    NONSENSE_MUTATION("Mutação sem Sentido");

    private final String description;

    ResultType(String description) {
        this.description = description;
    }
}