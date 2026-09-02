package br.ufpi.biocompiler.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.ReadingFrame;
import br.ufpi.biocompiler.models.StartCodon;
import br.ufpi.biocompiler.utils.DetermineReadingFrameImpl;

@Service
public class StartCodonService implements DetermineReadingFrameImpl{
    
    private static final String START_CODON = "ATG";

    public Optional<StartCodon> findStartCodon(String sequence){
        for(int position = 0; position <= sequence.length() - 3; position++){
            String codon = sequence.substring(position, position + 3);

            if(START_CODON.equals(codon)){
                ReadingFrame readingFrame = determineReadingFrame(position);

                if(readingFrame == ReadingFrame.FRAME_0){
                    return Optional.of(new StartCodon(position, codon, readingFrame));
                }
            }
        }
        return Optional.empty();
    }
}