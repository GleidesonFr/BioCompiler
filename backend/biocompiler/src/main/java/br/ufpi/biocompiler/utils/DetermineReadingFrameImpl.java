package br.ufpi.biocompiler.utils;

import br.ufpi.biocompiler.errors.FrameInvalidException;
import br.ufpi.biocompiler.models.ReadingFrame;

public interface DetermineReadingFrameImpl {
    default ReadingFrame determineReadingFrame(int position){
        return switch(position % 3){
            case 0 -> ReadingFrame.FRAME_0;
            case 1 -> ReadingFrame.FRAME_1;
            case 2 -> ReadingFrame.FRAME_2;
            default -> throw new FrameInvalidException();
        };
    }    
}
