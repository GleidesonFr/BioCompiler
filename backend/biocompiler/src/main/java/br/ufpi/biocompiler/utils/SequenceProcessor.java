package br.ufpi.biocompiler.utils;

import br.ufpi.biocompiler.models.Analysis;

public interface SequenceProcessor {
    Analysis process(String sequence);
}
