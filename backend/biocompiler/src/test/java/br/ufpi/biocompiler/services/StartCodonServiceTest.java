package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.ReadingFrame;
import br.ufpi.biocompiler.models.StartCodon;

public class StartCodonServiceTest {

    StartCodonService startCodonService = new StartCodonService();
    
    @Test
    void shouldFindStartCodon() {

        String sequence = "GGGGAAATGGCC";

        Optional<StartCodon> result =
                startCodonService.findStartCodon(sequence);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().getPosition());
        assertEquals("ATG", result.get().getCodon());
        assertEquals(ReadingFrame.FRAME_0,
                result.get().getReadingFrame());
    }

    @Test
    void shouldNotFindStartCodonInFrame1() {

        String sequence = "AATGCCC";

        Optional<StartCodon> result =
                startCodonService.findStartCodon(sequence);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotFindStartCodonInFrame2() {

        String sequence = "AAATGCCC";

        Optional<StartCodon> result =
                startCodonService.findStartCodon(sequence);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreStartCodonsOutsideFrame0() {

        String sequence = "AATGCCATG";

        Optional<StartCodon> result =
                startCodonService.findStartCodon(sequence);

        assertTrue(result.isPresent());
        assertEquals(6, result.get().getPosition());
        assertEquals("ATG", result.get().getCodon());
        assertEquals(ReadingFrame.FRAME_0,
                result.get().getReadingFrame());
    }
}
