package br.ufpi.biocompiler.services;

import org.springframework.stereotype.Service;

@Service
public class TranscriptionService{

    private static final char THYMINE = 'T';
    private static final char URACIL = 'U';

    public String transcribe(String sequence){
        return sequence.toUpperCase().replace(THYMINE, URACIL);
    }
}