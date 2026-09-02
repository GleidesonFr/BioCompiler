package br.ufpi.biocompiler.services;

import org.springframework.stereotype.Service;

@Service
public class DNAValidatorService {
    
    public boolean isValidDNA(String sequence) {
        if(sequence == null || sequence.isEmpty()) {
            throw new IllegalArgumentException("A sequence de DNA não pode ser nula ou vazia.");
        }

        for(char nucleotide : sequence.toUpperCase().toCharArray()) {
            if(nucleotide != 'A' && nucleotide != 'T' && nucleotide != 'C' && nucleotide != 'G') {
                return false;
            }
        }

        return true;
    }
}