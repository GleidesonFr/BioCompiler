package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

public class DNAFileReaderServiceTest {
    private final DNAFileReaderService readerService = new DNAFileReaderService();

    @Test
    void shouldReadDnaSequencesFromFile() throws Exception {

        String content =
                """
                ATGCCCAAATGA
                ATGGCXAAATGA
                TTGACCTGA
                ATGAAATAG
                """;

        MockMultipartFile file =
                new MockMultipartFile(
                    "file",
                    "entrada.txt",
                    "text/plain",
                    content.getBytes(StandardCharsets.UTF_8)
                );

        List<String> sequences = readerService.readSequences(file);

        assertEquals(4, sequences.size());
        assertEquals("ATGCCCAAATGA", sequences.get(0));
        assertEquals("ATGGCXAAATGA", sequences.get(1));
        assertEquals("TTGACCTGA", sequences.get(2));
        assertEquals("ATGAAATAG", sequences.get(3));
    }
}
