package br.ufpi.biocompiler.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;

class AnalysisExportServiceTest {

    private final AnalysisExportService analysisExportService = new AnalysisExportService();

    @Test
    void shouldExportMatureMrnaForRnaOnlyHistory() throws Exception {
        Analysis rna = analysis(SequenceType.PRE_MRNA, "m7GpppCCUAUGGCAUGGCCUAC".repeat(1));

        String content = new String(
            analysisExportService.generateTxT(List.of(rna)).getContentAsByteArray(),
            StandardCharsets.UTF_8
        );

        assertEquals(
            "linha;status;resultado;mRNA_maduro\n"
                + "1;OK;Sequência correta;m7GpppCCUAUGGCAUGGCCUAC\n",
            content
        );
    }

    @Test
    void shouldIdentifyTheProcessedSequenceTypeInMixedHistory() throws Exception {
        Analysis dna = analysis(SequenceType.DNA, "AUGAAACCCUGA");
        Analysis rna = analysis(SequenceType.PRE_MRNA, "m7GpppCCUAUGGCAUGGCCUAC");

        String content = new String(
            analysisExportService.generateTxT(List.of(dna, rna)).getContentAsByteArray(),
            StandardCharsets.UTF_8
        );

        assertEquals(
            "linha;tipo;status;resultado;sequencia_processada\n"
                + "1;DNA;OK;Sequência correta;AUGAAACCCUGA\n"
                + "2;PRE_MRNA;OK;Sequência correta;m7GpppCCUAUGGCAUGGCCUAC\n",
            content
        );
    }

    private Analysis analysis(SequenceType sequenceType, String processedSequence) {
        Analysis analysis = new Analysis();
        analysis.setSequenceType(sequenceType);
        analysis.setResultType(ResultType.CORRECT);
        analysis.setMessage("Sequência correta");
        if (sequenceType == SequenceType.PRE_MRNA) {
            analysis.setMatureMrna(processedSequence);
        } else {
            analysis.setPreMrna(processedSequence);
        }
        return analysis;
    }
}
