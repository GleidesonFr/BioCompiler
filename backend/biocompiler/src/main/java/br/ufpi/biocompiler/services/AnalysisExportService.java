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
    private static final String RIBOSOME_HEADER = "linha;status;resultado;proteina";
    private static final String MIXED_HEADER = "linha;tipo;status;resultado;sequencia_processada";

    public Resource generateTxT(List<Analysis> analyses) {
        StringBuilder content = new StringBuilder();
        boolean hasDna = analyses.stream().anyMatch(analysis -> analysis.getSequenceType() == SequenceType.DNA);
        boolean hasRna = analyses.stream().anyMatch(analysis -> analysis.getSequenceType() == SequenceType.PRE_MRNA);
        boolean hasMatureMrna = analyses.stream().anyMatch(analysis -> analysis.getSequenceType() == SequenceType.MATURE_MRNA);

        int typeCount = (hasDna ? 1 : 0) + (hasRna ? 1 : 0) + (hasMatureMrna ? 1 : 0);
        boolean mixedTypes = typeCount > 1;

        content.append(getHeader(hasDna, hasRna, hasMatureMrna, mixedTypes)).append("\n");

        for(int i = 0; i < analyses.size(); i++) {
            Analysis analysis = analyses.get(i);

            content.append(i + 1).append(";");
            if (mixedTypes) {
                content.append(analysis.getSequenceType() != null ? analysis.getSequenceType().name() : "DNA")
                    .append(";");
            }

            content.append(getStatus(analysis)).append(";")
                .append(getResultado(analysis)).append(";")
                .append(getProcessedSequence(analysis)).append("\n");
        }

        return new ByteArrayResource(content.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String getHeader(boolean hasDna, boolean hasRna, boolean hasMatureMrna, boolean mixedTypes) {
        if (mixedTypes) {
            return MIXED_HEADER;
        }
        if (hasMatureMrna) {
            return RIBOSOME_HEADER;
        }
        return hasRna ? RNA_HEADER : DNA_HEADER;
    }

    private String getStatus(Analysis analysis) {
        if (analysis.getResultType() == ResultType.CORRECT) {
            return "OK";
        }
        return analysis.getResultType() == ResultType.ALTERNATIVE_SPLICING ? "AMBIGUO" : "ERRO";
    }

    private String getResultado(Analysis analysis) {
        if (analysis.getSequenceType() == SequenceType.MATURE_MRNA && analysis.getResultType() == ResultType.CORRECT) {
            return "CORRETO";
        }
        return analysis.getMessage() != null ? analysis.getMessage() : analysis.getResultType().getDescription();
    }

    private String getProcessedSequence(Analysis analysis) {
        if (analysis.getSequenceType() == SequenceType.MATURE_MRNA) {
            if (analysis.getResultType() != ResultType.CORRECT) {
                return "NÃO GERADA";
            }
            String protein = analysis.getProtein();
            return protein == null || protein.isBlank() ? "NÃO GERADA" : protein;
        }

        if (analysis.getResultType() != ResultType.CORRECT) {
            return "NÃO GERADO";
        }

        String processedSequence = analysis.getSequenceType() == SequenceType.PRE_MRNA
            ? analysis.getMatureMrna()
            : analysis.getPreMrna();
        return processedSequence == null || processedSequence.isBlank() ? "NÃO GERADO" : processedSequence;
    }
}
