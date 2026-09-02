package br.ufpi.biocompiler.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class AnalysisRequestTest {
    
    @Test
    void shouldCreateAnalysisRequest() {

        UUID sessionId = UUID.randomUUID();
        AnalysisRequest request = AnalysisRequest.from("ATGAAACCCTGA", sessionId);

        assertEquals("ATGAAACCCTGA", request.sequence());
        assertEquals(sessionId, request.sessionId());
    }

}
