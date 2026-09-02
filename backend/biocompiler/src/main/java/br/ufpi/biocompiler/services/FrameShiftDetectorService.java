package br.ufpi.biocompiler.services;

import org.springframework.stereotype.Service;

@Service
public class FrameShiftDetectorService {
    
    public boolean isFrameShift(String sequence){
        return sequence.length() % 3 != 0;
    }
}
