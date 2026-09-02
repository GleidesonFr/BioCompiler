package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TranscriptionServiceTest {
    
    private final TranscriptionService transcriptionService = new TranscriptionService();

    @Test
    void shouldTranscribeDNAToPreMRNA(){
        String dna = "ATGAAACCCTGA";

        String result = transcriptionService.transcribe(dna);

        assertEquals("AUGAAACCCUGA", result);
    }
}
