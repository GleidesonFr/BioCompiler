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
import br.ufpi.biocompiler.services.BioCompilerService;
import br.ufpi.biocompiler.services.DNAFileReaderService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final BioCompilerService bioCompilerService;
    private final DNAFileReaderService readerService;

    public AnalysisController(BioCompilerService bioCompilerService, DNAFileReaderService readerService){
        this.bioCompilerService = bioCompilerService;
        this.readerService = readerService;
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

        Map<ResultType, Long> stats = bioCompilerService.getStatistics(sessionId);

        long correct = stats.getOrDefault(ResultType.CORRECT, 0L);
        long invalidBase = stats.getOrDefault(ResultType.INVALID_BASE, 0L);
        long startMissing = stats.getOrDefault(ResultType.START_CODON_NOT_FOUND, 0L);
        long stopMissing = stats.getOrDefault(ResultType.STOP_CODON_NOT_FOUND, 0L);
        long frameShift = stats.getOrDefault(ResultType.FRAME_SHIFT, 0L);
        long nonsense = stats.getOrDefault(ResultType.NONSENSE_MUTATION, 0L);

        return ResponseEntity.ok(
            new AnalysisStatisticsResponse(
                correct,
                invalidBase,
                startMissing,
                stopMissing,
                frameShift,
                nonsense
            )
        );
    }    
    
}