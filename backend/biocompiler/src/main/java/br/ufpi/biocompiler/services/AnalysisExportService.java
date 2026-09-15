package br.ufpi.biocompiler.services;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;

@Service
public class AnalysisExportService {
    
    private static final String DNA_HEADER = "linha;status;resultado;pre_mRNA";
    private static final String RNA_HEADER = "linha;status;resultado;mRNA_maduro";
    private static final String MIXED_HEADER = "linha;tipo;status;resultado;sequencia_processada";

    public Resource generateTxT(List<Analysis> analyses) {
        StringBuilder content = new StringBuilder();
        boolean hasDna = analyses.stream().anyMatch(analysis -> analysis.getSequenceType() == SequenceType.DNA);
        boolean hasRna = analyses.stream().anyMatch(analysis -> analysis.getSequenceType() == SequenceType.PRE_MRNA);
        boolean mixedTypes = hasDna && hasRna;

        content.append(getHeader(hasDna, hasRna)).append("\n");

        for(int i = 0; i < analyses.size(); i++) {
            Analysis analysis = analyses.get(i);

            content.append(i + 1).append(";");
            if (mixedTypes) {
                content.append(analysis.getSequenceType() == SequenceType.PRE_MRNA ? "PRE_MRNA" : "DNA")
                    .append(";");
            }

            content.append(getStatus(analysis)).append(";")
                .append(analysis.getMessage()).append(";")
                .append(getProcessedSequence(analysis)).append("\n");
        }

        return new ByteArrayResource(content.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String getHeader(boolean hasDna, boolean hasRna) {
        if (hasDna && hasRna) {
            return MIXED_HEADER;
        }
        return hasRna ? RNA_HEADER : DNA_HEADER;
    }

    private String getStatus(Analysis analysis) {
        if (analysis.getResultType() == ResultType.CORRECT) {
            return "OK";
        }
        return analysis.getResultType() == ResultType.ALTERNATIVE_SPLICING ? "AMBIGUO" : "ERRO";
    }

    private String getProcessedSequence(Analysis analysis) {
        if (analysis.getResultType() != ResultType.CORRECT) {
            return "NÃO GERADO";
        }

        String processedSequence = analysis.getSequenceType() == SequenceType.PRE_MRNA
            ? analysis.getMatureMrna()
            : analysis.getPreMrna();
        return processedSequence == null || processedSequence.isBlank() ? "NÃO GERADO" : processedSequence;
    }
}
