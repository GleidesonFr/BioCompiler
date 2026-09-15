package br.ufpi.biocompiler.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.ufpi.biocompiler.dto.AnalysisRequest;
import br.ufpi.biocompiler.dto.AnalysisResponse;
import br.ufpi.biocompiler.dto.AnalysisStatisticsResponse;
import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.models.SequenceType;
import br.ufpi.biocompiler.services.AnalysisExportService;
import br.ufpi.biocompiler.services.BioCompilerService;
import br.ufpi.biocompiler.services.DNAFileReaderService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@Profile("!terminal")
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final BioCompilerService bioCompilerService;
    private final DNAFileReaderService readerService;
    private final AnalysisExportService exportService;

    public AnalysisController(BioCompilerService bioCompilerService, DNAFileReaderService readerService, AnalysisExportService exportService){
        this.bioCompilerService = bioCompilerService;
        this.readerService = readerService;
        this.exportService = exportService;
    }

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
       Analysis analysis = bioCompilerService.analyzeAndSave(request.sequence(), request.sessionId());

       return ResponseEntity.ok(AnalysisResponse.from(analysis));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AnalysisResponse>> getHistory(@PageableDefault(size = 10, sort = "analysisDate", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam UUID sessionId) {
        Page<Analysis> analysisPage = bioCompilerService.getHistory(pageable, sessionId);
        Page<AnalysisResponse> responsePage = analysisPage.map(AnalysisResponse::from);

        return ResponseEntity.ok(responsePage);
    }
    
    @PostMapping("/file")
    public ResponseEntity<List<AnalysisResponse>> analyzeFile(
        @RequestParam("file") MultipartFile file, @RequestParam("sessionId") UUID sessionId) throws IOException {
        
        List<String>sequences = readerService.readSequences(file);
        List<Analysis> analyses = bioCompilerService.analyzeAndSave(sequences, sessionId);
        List<AnalysisResponse> responses = analyses.stream()
            .map(AnalysisResponse::from).toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponse> getAnalysisById(@PathVariable UUID id) {
        Analysis analysis = bioCompilerService.getAnalysisById(id);
        return ResponseEntity.ok(AnalysisResponse.from(analysis));
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(@RequestParam UUID sessionId){
        bioCompilerService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history/stats")
    public ResponseEntity<AnalysisStatisticsResponse> getStatistics(@RequestParam UUID sessionId) {

        Map<SequenceType, Map<ResultType, Long>> statsBySequenceType = bioCompilerService
            .getStatisticsBySequenceType(sessionId);
        Map<ResultType, Long> dnaStats = statsBySequenceType.getOrDefault(SequenceType.DNA, Map.of());
        Map<ResultType, Long> rnaStats = statsBySequenceType.getOrDefault(SequenceType.PRE_MRNA, Map.of());

        long dnaCorrect = dnaStats.getOrDefault(ResultType.CORRECT, 0L);
        long rnaCorrect = rnaStats.getOrDefault(ResultType.CORRECT, 0L);
        long invalidBase = dnaStats.getOrDefault(ResultType.INVALID_BASE, 0L);
        long rnaInvalidBase = rnaStats.getOrDefault(ResultType.INVALID_BASE, 0L);
        long startMissing = dnaStats.getOrDefault(ResultType.START_CODON_NOT_FOUND, 0L);
        long stopMissing = dnaStats.getOrDefault(ResultType.STOP_CODON_NOT_FOUND, 0L);
        long frameShift = dnaStats.getOrDefault(ResultType.FRAME_SHIFT, 0L);
        long nonsense = dnaStats.getOrDefault(ResultType.NONSENSE_MUTATION, 0L);
        long fivePrimeSite = rnaStats.getOrDefault(ResultType.FIVE_PRIME_SITE_ERROR, 0L);
        long branchPoint = rnaStats.getOrDefault(ResultType.BRANCH_POINT_ERROR, 0L);
        long threePrimeSite = rnaStats.getOrDefault(ResultType.THREE_PRIME_SITE_ERROR, 0L);
        long incompleteIntron = rnaStats.getOrDefault(ResultType.INCOMPLETE_INTRON, 0L);
        long alternativeSplicing = rnaStats.getOrDefault(ResultType.ALTERNATIVE_SPLICING, 0L);

        return ResponseEntity.ok(
            new AnalysisStatisticsResponse(
                dnaCorrect + rnaCorrect,
                invalidBase,
                startMissing,
                stopMissing,
                frameShift,
                nonsense,
                dnaCorrect,
                rnaCorrect,
                rnaInvalidBase,
                fivePrimeSite,
                branchPoint,
                threePrimeSite,
                incompleteIntron,
                alternativeSplicing
            )
        );
    }
    
    @GetMapping("/history/export")
    public ResponseEntity<Resource> exportHistory(@RequestParam UUID sessionId) throws IOException {
        List<Analysis> analyses = bioCompilerService.getAllForExport(sessionId);
        Resource fileResource = exportService.generateTxT(analyses);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resultados.txt\"")
            .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
            .contentLength(fileResource.contentLength())
            .body(fileResource);
    }
    
}
