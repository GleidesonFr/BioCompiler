package br.ufpi.biocompiler.services;

import java.util.List;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.ReadingFrame;
import br.ufpi.biocompiler.models.StopCodon;

@Service
public class NonSenseMutationDetectorService {

    public boolean isNonSenseMutation(List<StopCodon> stopCodons, ReadingFrame startFrame){
        long stopCount = stopCodons.stream().filter(stopCodon -> stopCodon.getReadingFrame() == startFrame).count();

        return stopCount > 1;
    }
}
