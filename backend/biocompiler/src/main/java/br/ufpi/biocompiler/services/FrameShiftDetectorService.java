package br.ufpi.biocompiler.services;

import org.springframework.stereotype.Service;

@Service
public class FrameShiftDetectorService {
    
    public boolean isFrameShift(String sequence, int startPosition){
        int baseAfterStart = sequence.length() - startPosition;

        return baseAfterStart % 3 != 0;
    }
}
