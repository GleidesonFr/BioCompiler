package br.ufpi.biocompiler.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import br.ufpi.biocompiler.repositories.AnalysisRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AnalysisControllerTest {

    @Autowired
    private AnalysisRepository analysisRepository;
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAnalyzeDnaSequence() throws Exception {
        analysisRepository.deleteAll();

        String request = 
                """
                {
                    "sequence": "ATGAAACCCTGA"
                }
                """;

        mockMvc.perform(post("/api/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.originalSequence").value("ATGAAACCCTGA"))
        .andExpect(jsonPath("$.result").value("CORRECT"))
        .andExpect(jsonPath("$.positionStart").value(0))
        .andExpect(jsonPath("$.positionStop").value(9))
        .andExpect(jsonPath("$.readingFrame").value("FRAME_0"))
        .andExpect(jsonPath("$.codingRegion").value("ATGAAACCCTGA"))
        .andExpect(jsonPath("$.preMrna").value("AUGAAACCCUGA"))
        .andExpect(jsonPath("$.message").value("Sequência correta"));
    }

    @Test
    void shouldReturnFrameShiftResult() throws Exception {
        analysisRepository.deleteAll();

        String request =
            """
            {
                "sequence": "ATGAAAATGA"
            }
            """;

        mockMvc.perform(post("/api/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("FRAME_SHIFT"))
        .andExpect(jsonPath("$.message").value("BUG - frameshift"));
    }

    @Test
    void shouldReturnAnalysisHistory() throws Exception {
        analysisRepository.deleteAll();
        String firstRequest =
                """
                {
                    "sequence": "ATGAAACCCTGA"
                }
                """;

        String secondRequest =
                """
                {
                    "sequence": "ATGAAA"
                }
                """;

        mockMvc.perform(post("/api/analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest)
        ).andExpect(status().isOk());

        mockMvc.perform(post("/api/analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequest)
        ).andExpect(status().isOk());

        mockMvc.perform(get("/api/analysis/history")
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].originalSequence").value("ATGAAA"))
        .andExpect(jsonPath("$[1].originalSequence").value("ATGAAACCCTGA"));
    }

    @Test
    void shouldAnalyzeDnaFile() throws Exception {
        analysisRepository.deleteAll();

        String content = 
                """
                ATGCCCAAATGA
                ATGAAACCCTGA
                """;

        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "entrada.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
            );

        mockMvc.perform(multipart("/api/analysis/file").file(file)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].originalSequence").value("ATGCCCAAATGA"))
        .andExpect(jsonPath("$[1].originalSequence").value("ATGAAACCCTGA"));
    }

    @Test
    void shouldRejectEmptySequence() throws Exception {

        String request =
                """
                {
                    "sequence": ""
                }
                """;

        mockMvc.perform(post("/api/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
        ).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
        .value("A sequence de DNA não pode ser nula ou vazia."));
    }

    @Test
    void shouldRejectNullSequence() throws Exception {

        String request =
            """
            {
                "sequence": null
            }
            """;

        mockMvc.perform(post("/api/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
        ).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
        .value("A sequence de DNA não pode ser nula ou vazia."));
    }

    @Test
    void shouldRejectRequestWithoutSequence() throws Exception {

        String request =
                """
                {}
                """;

        mockMvc.perform(post("/api/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
        ).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
        .value("A sequence de DNA não pode ser nula ou vazia."));
    }

    @Test
    void shouldReturnInvalidBaseResult() throws Exception {

        String request = """
                {
                    "sequence": "ATGAAAXCCTGA"
                }
                """;

        mockMvc.perform(
                post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result")
                .value("INVALID_BASE"))
        .andExpect(jsonPath("$.message")
                .value("BUG - base inválida"));
    }

    @Test
    void shouldRejectEmptyFile() throws Exception {

        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "entrada.txt",
                "text/plain",
                new byte[0]
            );

        mockMvc.perform(multipart("/api/analysis/file").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("O arquivo não pode ser vazio"));
    }

    @Test
    void shouldRejectFileWithoutSequences() throws Exception {

        String content = """
                
                
                """;

        MockMultipartFile file =
            new MockMultipartFile(
                "file",
                "entrada.txt",
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
            );

        mockMvc.perform(multipart("/api/analysis/file").file(file))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message")
        .value("A sequence de DNA não pode ser nula ou vazia."));
    }
}
