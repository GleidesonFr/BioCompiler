package br.ufpi.biocompiler.services;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;

@Service
public class AnalysisExportService {
    
    private static final String HEADER = "linha;status;resultado;pre_mRNA";

    public Resource generateTxT(List<Analysis> analyses) {
        StringBuilder content = new StringBuilder();
        content.append(HEADER).append("\n");

        for(int i = 0; i < analyses.size(); i++) {
            Analysis analysis = analyses.get(i);

            content.append(i + 1).append(";")
                .append(getStatus(analysis)).append(";")
                .append(analysis.getMessage()).append(";")
                .append(getPreMrna(analysis)).append("\n");
        }

        return new ByteArrayResource(content.toString().getBytes());
    }

    private String getStatus(Analysis analysis) {
        return analysis.getResultType() == ResultType.CORRECT ? "OK" : "ERRO";
    }

    private String getPreMrna(Analysis analysis) {
        if(analysis.getResultType() == ResultType.CORRECT && analysis.getPreMrna() != null && !analysis.getPreMrna().isBlank()) {
            return analysis.getPreMrna();
        }
        return "NÃO GERADO";
    }
}
