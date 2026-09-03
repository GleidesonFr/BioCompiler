package br.ufpi.biocompiler.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.ReadingFrame;
import br.ufpi.biocompiler.models.StopCodon;
import br.ufpi.biocompiler.utils.DetermineReadingFrameImpl;

@Service
public class StopCodonService implements DetermineReadingFrameImpl{
    
    private static final String[] STOP_CODONS = {"TAA", "TAG", "TGA"};

    public List<StopCodon> findStopCodon(String sequence, int startPosition){
        List<StopCodon> stopCodons = new ArrayList<>();
        
        for(int position = startPosition + 3; position <= sequence.length() - 3; position += 3){
            String codon = sequence.substring(position, position + 3);

            if(isStopCodon(codon)){
                ReadingFrame readingFrame = determineReadingFrame(position);
                stopCodons.add(new StopCodon(position, codon, readingFrame));
            }
        }

        return stopCodons;
    }

    protected boolean isStopCodon(String codon){
        return Arrays.stream(STOP_CODONS)
        .anyMatch(stopCodon -> stopCodon.equals(codon));
    }


}
