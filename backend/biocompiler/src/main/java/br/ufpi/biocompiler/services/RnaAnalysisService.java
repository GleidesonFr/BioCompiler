package br.ufpi.biocompiler.services;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;
import br.ufpi.biocompiler.utils.SequenceProcessor;

@Service
public class RnaAnalysisService implements SequenceProcessor {

    private static final int MIN_BRANCH_DISTANCE = 10;
    private static final int MAX_BRANCH_DISTANCE = 30;
    private static final int FIVE_PRIME_SITE_POSITION = 12;
    private static final int MIN_COMPLETE_SEQUENCE_LENGTH = 36;
    private static final String CAP_5 = "m7Gppp";
    private static final String POLY_A_TAIL = "A".repeat(100);

    private final AnalysisMessageService analysisMessageService;

    public RnaAnalysisService(AnalysisMessageService analysisMessageService) {
        this.analysisMessageService = analysisMessageService;
    }

    @Override
    public Analysis process(String sequence) {
        Analysis analysis = new Analysis();
        analysis.setOriginalSequence(sequence);
        analysis.setSequenceType(SequenceType.PRE_MRNA);
        analysis.setAnalysisDate(LocalDateTime.now());

        String normalizedSequence = sequence.toUpperCase();
        if (!normalizedSequence.matches("[ACGU]+")) {
            return finish(analysis, ResultType.INVALID_BASE);
        }

        int fivePrimeSitePosition = findFivePrimeSite(normalizedSequence);
        if (fivePrimeSitePosition < 0) {
            return finish(analysis, ResultType.FIVE_PRIME_SITE_ERROR);
        }

        Set<Intron> validIntrons = findValidIntrons(normalizedSequence, fivePrimeSitePosition);
        if (validIntrons.size() > 1) {
            return finish(analysis, ResultType.ALTERNATIVE_SPLICING);
        }

        if (validIntrons.size() == 1) {
            Intron intron = validIntrons.iterator().next();
            String processedSequence = normalizedSequence.substring(0, intron.start())
                + normalizedSequence.substring(intron.end() + 2);
            analysis.setMatureMrna(CAP_5 + processedSequence + POLY_A_TAIL);
            return finish(analysis, ResultType.CORRECT);
        }

        return finish(analysis, diagnoseInvalidIntron(normalizedSequence, fivePrimeSitePosition));
    }

    private int findFivePrimeSite(String sequence) {
        if (usesProfessorCanonicalFormat(sequence)) {
            return sequence.length() >= FIVE_PRIME_SITE_POSITION + 2
                && sequence.startsWith("GU", FIVE_PRIME_SITE_POSITION)
                    ? FIVE_PRIME_SITE_POSITION
                    : -1;
        }

        return sequence.indexOf("GU");
    }

    private boolean usesProfessorCanonicalFormat(String sequence) {
        return sequence.length() == 26 || sequence.length() == 48 || sequence.length() == 62;
    }

    private Set<Intron> findValidIntrons(String sequence, int start) {
        Set<Intron> introns = new LinkedHashSet<>();
        if (usesProfessorCanonicalFormat(sequence)) {
            addValidIntrons(sequence, start, introns);
            return introns;
        }

        for (int candidateStart = sequence.indexOf("GU"); candidateStart >= 0;
            candidateStart = sequence.indexOf("GU", candidateStart + 1)) {
            addValidIntrons(sequence, candidateStart, introns);
        }

        return introns;
    }

    private void addValidIntrons(String sequence, int start, Set<Intron> introns) {
        for (int end = sequence.indexOf("AG", start + 2); end >= 0; end = sequence.indexOf("AG", end + 1)) {
            if (hasValidBranchPoint(sequence, start, end)) {
                introns.add(new Intron(start, end));
            }
        }
    }

    private boolean hasValidBranchPoint(String sequence, int intronStart, int intronEnd) {
        int firstBranchPosition = Math.max(intronStart + 2, intronEnd - MAX_BRANCH_DISTANCE);
        int lastBranchPosition = intronEnd - MIN_BRANCH_DISTANCE;
        for (int position = firstBranchPosition; position <= lastBranchPosition; position++) {
            if (sequence.charAt(position) == 'A') {
                return true;
            }
        }
        return false;
    }

    private ResultType diagnoseInvalidIntron(String sequence, int intronStart) {
        int firstAg = sequence.indexOf("AG", intronStart + 2);
        if (firstAg >= 0) {
            return ResultType.BRANCH_POINT_ERROR;
        }

        if (usesProfessorCanonicalFormat(sequence)) {
            return sequence.length() < MIN_COMPLETE_SEQUENCE_LENGTH
                ? ResultType.INCOMPLETE_INTRON
                : ResultType.THREE_PRIME_SITE_ERROR;
        }

        return hasPotentialThreePrimeSite(sequence, intronStart)
            ? ResultType.THREE_PRIME_SITE_ERROR
            : ResultType.INCOMPLETE_INTRON;
    }

    private boolean hasPotentialThreePrimeSite(String sequence, int intronStart) {
        int branchPosition = sequence.length() - MIN_BRANCH_DISTANCE;
        return branchPosition >= intronStart + 2
            && sequence.charAt(branchPosition) == 'A';
    }

    private Analysis finish(Analysis analysis, ResultType resultType) {
        analysis.setResultType(resultType);
        analysis.setMessage(analysisMessageService.generateMessage(resultType));
        return analysis;
    }

    private record Intron(int start, int end) {
    }
}
