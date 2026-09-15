package br.ufpi.biocompiler.services;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.ResultType;

@Service
public class AnalysisMessageService {
    
    public String generateMessage(ResultType resultType){
        return switch(resultType){
            case CORRECT -> "Sequência correta";
            case INVALID_BASE -> "BUG - base inválida";
            case START_CODON_NOT_FOUND -> "BUG - START ausente";
            case STOP_CODON_NOT_FOUND -> "BUG - STOP ausente";
            case FRAME_SHIFT -> "BUG - frameshift";
            case NONSENSE_MUTATION -> "BUG - nonsense / STOP prematuro";
            case FIVE_PRIME_SITE_ERROR -> "BUG - sítio 5'";
            case BRANCH_POINT_ERROR -> "BUG - branch point";
            case THREE_PRIME_SITE_ERROR -> "BUG - sítio 3'";
            case INCOMPLETE_INTRON -> "BUG - íntron incompleto";
            case ALTERNATIVE_SPLICING -> "AMBÍGUO - splicing alternativo";
        };
    }
}
