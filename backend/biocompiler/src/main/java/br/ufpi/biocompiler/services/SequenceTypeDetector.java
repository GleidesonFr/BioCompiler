package br.ufpi.biocompiler.services;

import br.ufpi.biocompiler.models.SequenceType;
import org.springframework.stereotype.Component;

@Component
public class SequenceTypeDetector {
    
    public SequenceType detect(String sequence) {

        String normalizedSequence = normalize(sequence);

        if (normalizedSequence == null || normalizedSequence.isBlank()) {
            throw new IllegalArgumentException("A sequência não pode ser nula ou vazia.");
        }

        if (isMatureMrna(normalizedSequence)) {
            return SequenceType.MATURE_MRNA;
        }

        normalizedSequence = normalizedSequence.toUpperCase();
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

        throw new IllegalArgumentException("Não foi possível identificar a sequência como DNA, pré-mRNA ou mRNA maduro.");
    }

    String normalize(String sequence) {
        return sequence == null ? null : sequence.replaceAll("\\s+", "");
    }

    private boolean isMatureMrna(String sequence) {
        if (sequence == null) {
            return false;
        }

        String trimmed = sequence.toUpperCase();

        boolean hasCap = trimmed.startsWith("M7GPPP");

        int trailingAs = 0;
        int len = trimmed.length();
        while (trailingAs < len && trimmed.charAt(len - 1 - trailingAs) == 'A') {
            trailingAs++;
        }
        boolean hasPolyA = (trailingAs >= 100);
        return hasCap || hasPolyA;
    }
}
