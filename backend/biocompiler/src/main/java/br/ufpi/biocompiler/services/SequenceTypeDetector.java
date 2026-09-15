package br.ufpi.biocompiler.services;

import br.ufpi.biocompiler.models.SequenceType;
import org.springframework.stereotype.Component;

@Component
public class SequenceTypeDetector {
    
    public SequenceType detect(String sequence) {

        if (sequence == null || sequence.isBlank()) {
            throw new IllegalArgumentException("A sequência não pode ser nula ou vazia.");
        }

        String normalizedSequence = sequence.toUpperCase();
        boolean containsT = normalizedSequence.contains("T");
        boolean containsU = normalizedSequence.contains("U");

        if (containsT && containsU) {
            throw new IllegalArgumentException("A sequência não pode conter T e U ao mesmo tempo.");
        }

        if (containsT) {
            return SequenceType.DNA;
        }

        if (containsU) {
            return SequenceType.PRE_MRNA;
        }

        throw new IllegalArgumentException("Não foi possível identificar a sequência como DNA ou pré-mRNA.");
    }
}
