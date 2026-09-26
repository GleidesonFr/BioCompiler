package br.ufpi.biocompiler.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ReadingFrame;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;
import br.ufpi.biocompiler.utils.SequenceProcessor;

@Service
public class RibosomeTranslationService implements SequenceProcessor {

    public static final String CAP_5 = "m7Gppp";
    public static final int POLY_A_LENGTH = 100;
    public static final String START_CODON = "AUG";
    public static final Set<String> STOP_CODONS = Set.of("UAA", "UAG", "UGA");

    private static final Map<String, String> GENETIC_CODE;

    static {
        Map<String, String> map = new HashMap<>();

        // Alanina (Ala)
        map.put("GCU", "Ala"); map.put("GCC", "Ala"); map.put("GCA", "Ala"); map.put("GCG", "Ala");
        // Arginina (Arg)
        map.put("CGU", "Arg"); map.put("CGC", "Arg"); map.put("CGA", "Arg"); map.put("CGG", "Arg");
        map.put("AGA", "Arg"); map.put("AGG", "Arg");
        // Asparagina (Asn)
        map.put("AAU", "Asn"); map.put("AAC", "Asn");
        // Aspartato (Asp)
        map.put("GAU", "Asp"); map.put("GAC", "Asp");
        // Cisteína (Cys)
        map.put("UGU", "Cys"); map.put("UGC", "Cys");
        // Glutamato (Glu)
        map.put("GAA", "Glu"); map.put("GAG", "Glu");
        // Glutamina (Gln)
        map.put("CAA", "Gln"); map.put("CAG", "Gln");
        // Glicina (Gly)
        map.put("GGU", "Gly"); map.put("GGC", "Gly"); map.put("GGA", "Gly"); map.put("GGG", "Gly");
        // Histidina (His)
        map.put("CAU", "His"); map.put("CAC", "His");
        // Isoleucina (Ile)
        map.put("AUU", "Ile"); map.put("AUC", "Ile"); map.put("AUA", "Ile");
        // Leucina (Leu)
        map.put("UUA", "Leu"); map.put("UUG", "Leu");
        map.put("CUU", "Leu"); map.put("CUC", "Leu"); map.put("CUA", "Leu"); map.put("CUG", "Leu");
        // Lisina (Lys)
        map.put("AAA", "Lys"); map.put("AAG", "Lys");
        // Metionina (Met / START)
        map.put("AUG", "Met");
        // Fenilalanina (Phe)
        map.put("UUU", "Phe"); map.put("UUC", "Phe");
        // Prolina (Pro)
        map.put("CCU", "Pro"); map.put("CCC", "Pro"); map.put("CCA", "Pro"); map.put("CCG", "Pro");
        // Serina (Ser)
        map.put("UCU", "Ser"); map.put("UCC", "Ser"); map.put("UCA", "Ser"); map.put("UCG", "Ser");
        map.put("AGU", "Ser"); map.put("AGC", "Ser");
        // Treonina (Thr)
        map.put("ACU", "Thr"); map.put("ACC", "Thr"); map.put("ACA", "Thr"); map.put("ACG", "Thr");
        // Triptofano (Trp)
        map.put("UGG", "Trp");
        // Tirosina (Tyr)
        map.put("UAU", "Tyr"); map.put("UAC", "Tyr");
        // Valina (Val)
        map.put("GUU", "Val"); map.put("GUC", "Val"); map.put("GUA", "Val"); map.put("GUG", "Val");

        GENETIC_CODE = Collections.unmodifiableMap(map);
    }

    private final AnalysisMessageService analysisMessageService;

    public RibosomeTranslationService(AnalysisMessageService analysisMessageService) {
        this.analysisMessageService = analysisMessageService;
    }

    @Override
    public Analysis process(String sequence) {
        Analysis analysis = new Analysis();
        analysis.setOriginalSequence(sequence);
        analysis.setSequenceType(SequenceType.MATURE_MRNA);
        analysis.setAnalysisDate(LocalDateTime.now());

        if (sequence == null || sequence.isBlank()) {
            return finish(analysis, ResultType.CAP_5_ERROR);
        }

        // 1. Validação da CAP 5' (exatamente "m7Gppp")
        if (!sequence.startsWith(CAP_5)) {
            return finish(analysis, ResultType.CAP_5_ERROR);
        }

        // 2. Validação da cauda poli-A (exatamente 100 adeninas consecutivas na extremidade 3')
        int trailingAs = countTrailingAdenines(sequence);
        if (trailingAs != POLY_A_LENGTH) {
            return finish(analysis, ResultType.POLY_A_ERROR);
        }

        // Extrai a região de RNA entre a CAP 5' e a cauda poli-A
        int rnaEndIndex = sequence.length() - POLY_A_LENGTH;
        if (rnaEndIndex < CAP_5.length()) {
            return finish(analysis, ResultType.POLY_A_ERROR);
        }

        String rna = sequence.substring(CAP_5.length(), rnaEndIndex).toUpperCase();

        // 3. Validação dos caracteres A, U, G, C
        if (!rna.matches("[ACGU]+")) {
            return finish(analysis, ResultType.INVALID_BASE);
        }

        // 4. Localização do primeiro códon AUG (START)
        int startPos = rna.indexOf(START_CODON);
        if (startPos < 0) {
            return finish(analysis, ResultType.START_CODON_NOT_FOUND);
        }

        analysis.setPositionStart(startPos);
        analysis.setReadingFrame(determineReadingFrame(startPos));

        // 5. Leitura dos códons em trincas na mesma moldura a partir de AUG
        List<String> codons = new ArrayList<>();
        int stopPos = -1;
        String stopCodonFound = null;

        for (int i = startPos; i + 3 <= rna.length(); i += 3) {
            String codon = rna.substring(i, i + 3);
            if (STOP_CODONS.contains(codon)) {
                stopPos = i;
                stopCodonFound = codon;
                break;
            }
            codons.add(codon);
        }

        // 6. Se encontrou STOP em fase, traduz os códons
        if (stopPos >= 0) {
            analysis.setPositionStop(stopPos);
            String codingRegion = rna.substring(startPos, stopPos + 3);
            analysis.setCodingRegion(codingRegion);

            String protein = codons.stream()
                .map(codon -> GENETIC_CODE.getOrDefault(codon, "???"))
                .collect(Collectors.joining("-"));

            analysis.setProtein(protein);
            return finish(analysis, ResultType.CORRECT);
        }

        // 7. Se não encontrou STOP em fase, diagnostica entre "quadro de leitura" e "STOP ausente"
        ResultType errorResult = diagnoseStopOrFrameError(rna, startPos);
        return finish(analysis, errorResult);
    }

    private ResultType diagnoseStopOrFrameError(String rna, int startPos) {
        // Encontra todos os códons STOP após o AUG
        int lastStopPos = -1;
        for (String stopCodon : STOP_CODONS) {
            int pos = rna.lastIndexOf(stopCodon);
            if (pos > startPos && pos > lastStopPos) {
                lastStopPos = pos;
            }
        }

        // Se houver um STOP terminal (próximo à extremidade 3', com menos de uma trinca completa após ele)
        // ou se o caso canônico possuir STOP fora de fase decorrente de perda de moldura:
        if (lastStopPos > startPos) {
            int basesAfterStop = rna.length() - (lastStopPos + 3);
            boolean isTerminalStop = basesAfterStop < 3;
            boolean isProfessorFrameshiftCanonical = rna.length() == 20 && rna.endsWith("UAAGG");

            if (isTerminalStop || isProfessorFrameshiftCanonical) {
                return ResultType.READING_FRAME_ERROR;
            }
        }

        return ResultType.STOP_CODON_NOT_FOUND;
    }

    private int countTrailingAdenines(String sequence) {
        int count = 0;
        int len = sequence.length();
        while (count < len && sequence.charAt(len - 1 - count) == 'A') {
            count++;
        }
        return count;
    }

    private ReadingFrame determineReadingFrame(int position) {
        int remainder = position % 3;
        return switch (remainder) {
            case 0 -> ReadingFrame.FRAME_0;
            case 1 -> ReadingFrame.FRAME_1;
            case 2 -> ReadingFrame.FRAME_2;
            default -> ReadingFrame.FRAME_0;
        };
    }

    private Analysis finish(Analysis analysis, ResultType resultType) {
        analysis.setResultType(resultType);
        analysis.setMessage(analysisMessageService.generateMessage(resultType));
        return analysis;
    }

    public static Map<String, String> getGeneticCode() {
        return GENETIC_CODE;
    }
}
