package br.ufpi.biocompiler.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StartCodon {
    private int position;
    private String codon;
    private ReadingFrame readingFrame;
}
