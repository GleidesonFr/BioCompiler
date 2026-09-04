package br.ufpi.biocompiler.CLI;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.ufpi.biocompiler.models.Analysis;
import br.ufpi.biocompiler.models.ResultType;
import br.ufpi.biocompiler.services.DNAAnalysisService;
import br.ufpi.biocompiler.services.DNAFileReaderService;

@Component
@Profile("terminal")
public class BioCompilerTerminal implements CommandLineRunner{

    private final DNAAnalysisService dnaAnalysisService;
    private final DNAFileReaderService dnaFileReaderService;
    private final Scanner scanner = new Scanner(System.in);

    public BioCompilerTerminal(DNAAnalysisService dnaAnalysisService, DNAFileReaderService dnaFileReaderService) {
        this.dnaAnalysisService = dnaAnalysisService;
        this.dnaFileReaderService = dnaFileReaderService;
    }

    @Override
    public void run(String... args) throws Exception {
        showHeader();
        
        boolean executing = true;

        while(executing){
            showMenu();

            String option = scanner.nextLine().trim();

            switch(option){
                case "1" -> analyzeSequence();
                case "2" -> analyzeFile();
                case "0" -> {
                    System.out.println();
                    System.out.println("Encerrando o BioCompiler 1.0...");
                    System.out.println("Até logo!");
                    executing = false;
                }
                default -> {
                    System.out.println();
                    System.out.println("Opção inválida.");
                    System.out.println("Por favor, escolha uma opção entre 1 e 3");
                }
            }
        }

        scanner.close();
    }

    private void showHeader(){
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║              BIOCOMPILER 1.0                 ║");
        System.out.println("║               DNA TRANSCRIBER                ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();        
    }

    private void showMenu(){
        System.out.println();
        System.out.println("┌──────────────────────────────────────────────┐");
        System.out.println("│                    MENU                      │");
        System.out.println("├──────────────────────────────────────────────┤");
        System.out.println("│ 1 - Analisar sequência                       │");
        System.out.println("│ 2 - Analisar arquivo                         │");
        System.out.println("│ 0 - Sair                                     │");
        System.out.println("└──────────────────────────────────────────────┘");
        System.out.print("Opção: ");        
    }

    private void analyzeSequence(){
        System.out.println();
        System.out.println("════════════════════════════════════════════════");
        System.out.println("              ANÁLISE INDIVIDUAL");
        System.out.println("════════════════════════════════════════════════");
        System.out.println();

        System.out.print("Digite a sequência de DNA: ");

        String sequence = scanner.nextLine().trim();

        if (sequence.isEmpty()) {
            System.out.println();
            System.out.println("Erro: a sequência não pode estar vazia.");
            return;
        }

        Analysis analysis = dnaAnalysisService.analyze(sequence);

        System.out.println();

        showResults(analysis, 1);

        System.out.println();
        System.out.println("Pressione ENTER para continuar...");
        scanner.nextLine();        
    }

    private void analyzeFile(){
        System.out.println();
        System.out.println("════════════════════════════════════════════════");
        System.out.println("                 ANÁLISE EM LOTE");
        System.out.println("════════════════════════════════════════════════");
        System.out.println();

        System.out.print("Digite o caminho do arquivo: ");

        String caminho = scanner.nextLine().trim();

        if (caminho.isEmpty()) {
            System.out.println();
            System.out.println("Erro: o caminho não pode estar vazio.");
            return;
        }

        Path path = Paths.get(caminho);

        if (!Files.exists(path)) {
            System.out.println();
            System.out.println("Erro: arquivo não encontrado.");
            return;
        }

        if (!Files.isRegularFile(path)) {
            System.out.println();
            System.out.println("Erro: o caminho informado não é um arquivo.");
            return;
        }
        
        try{
            List<String> sequences = dnaFileReaderService.readSequences(Files.newInputStream(path));  
            List<Analysis> analyses = new ArrayList<>();

            System.out.println();
            System.out.println("Processando " + sequences.size() + " entrada(s)...");
            System.out.println(); 
            
            int entrada = 1;

            for(String sequence : sequences){
                Analysis analysis = dnaAnalysisService.analyze(sequence);
                analyses.add(analysis);

                showResults(analysis, entrada);

                System.out.println();
                entrada++;
            }

            showResume(analyses);
            askExport(analyses);

            System.out.println();
            System.out.println("Pressione ENTER para continuar...");
            scanner.nextLine();            
        }catch(IOException e){
            System.out.println();
            System.out.println("Erro ao ler o arquivo:");
            System.out.println(e.getMessage());            
        }catch(Exception e){
            System.out.println();
            System.out.println("Erro ao processar o arquivo:");
            System.out.println(e.getMessage());
        }
    }

    private void showResults(Analysis analysis, int inputNumber){
        System.out.println("========================================");
        System.out.println("BIOCOMPILER 1.0 - DNA TRANSCRIBER");
        System.out.println("========================================");        
        
        System.out.println("ENTRADA: " + inputNumber);

        ResultType resultType = analysis.getResultType();

        if(resultType == ResultType.CORRECT){
            System.out.println("STATUS: CORRETO");
            System.out.println("Bases: OK");
            System.out.println("START: ATG - OK");
            System.out.println("Quadro de leitura: OK");  
            
            if(analysis.getPositionStop() != null){
                System.out.println("STOP: " + getStopCodon(analysis) + " - OK");
            } else {
                System.out.println("STOP: OK");
            }

            System.out.println("Transcrição: OK");
            System.out.println("pré-mRNA: " + getValue(analysis.getPreMrna(), "NÃO GERADO"));
        }else{
            System.out.println("STATUS: ERRO");
            System.out.println("TIPO: " + getResultMessage(resultType));
            System.out.println("pré-mRNA: NÃO GERADO");
        }

        System.out.println("----------------------------------------");

    }

    private String getStopCodon(Analysis analysis){
        String codingRegion = analysis.getCodingRegion();

        if(codingRegion == null || codingRegion.length() < 3){
            return "STOP";
        }

        return codingRegion.substring(codingRegion.length() - 3);
    }

    private String getResultMessage(ResultType resultType){
        return switch (resultType) {

            case INVALID_BASE ->
                    "BUG - base inválida";

            case START_CODON_NOT_FOUND ->
                    "BUG - START ausente";

            case STOP_CODON_NOT_FOUND ->
                    "BUG - STOP ausente";

            case FRAME_SHIFT ->
                    "BUG - frameshift";

            case NONSENSE_MUTATION ->
                    "BUG - nonsense / STOP prematuro";

            case CORRECT ->
                    "CORRETO";
        };        
    }

    private String getValue(String value, String defaultValue){
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void showResume(List<Analysis> analyses){
        Map<ResultType, Integer> counts = new EnumMap<>(ResultType.class);

        for(ResultType type : ResultType.values()){
            counts.put(type, 0);
        }

        for(Analysis analysis : analyses){
            ResultType type = analysis.getResultType();
            counts.put(type, counts.get(type) + 1);
        }

        System.out.println();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║                 RESUMO                 ║");
        System.out.println("╠════════════════════════════════════════╣");        
        System.out.printf(
                "║ CORRETO:       %-24d║%n",
                counts.get(ResultType.CORRECT)
        );

        System.out.printf(
                "║ BASE INVÁLIDA: %-24d║%n",
                counts.get(ResultType.INVALID_BASE)
        );

        System.out.printf(
                "║ START AUSENTE: %-24d║%n",
                counts.get(ResultType.START_CODON_NOT_FOUND)
        );

        System.out.printf(
                "║ STOP AUSENTE:  %-24d║%n",
                counts.get(ResultType.STOP_CODON_NOT_FOUND)
        );

        System.out.printf(
                "║ FRAMESHIFT:    %-24d║%n",
                counts.get(ResultType.FRAME_SHIFT)
        );

        System.out.printf(
                "║ NONSENSE:      %-24d║%n",
                counts.get(ResultType.NONSENSE_MUTATION)
        ); 
        
        System.out.println("╠════════════════════════════════════════╣");

        System.out.printf(
                "║ TOTAL:         %-24d║%n",
                analyses.size()
        );

        System.out.println("╚════════════════════════════════════════╝");
    }

    private void askExport(List<Analysis> analyses){
        System.out.println();
        System.out.print("Deseja exportar os resultados? [S/N]: ");

        String answer = scanner.nextLine().trim().toUpperCase();

        if(answer.equals("S")){
            System.out.print("Caminho para salvar o resultado [resultados.txt]: ");
            String outputPath = scanner.nextLine().trim();

            if(outputPath.isEmpty()){
                outputPath = "resultados.txt";
            }

            exportResults(analyses, outputPath);
        }
    }

    private void exportResults(List<Analysis> analyses, String fileName){
        Path path = Paths.get(fileName);

        try {
            Path parent = path.getParent();

            if(parent != null){
                Files.createDirectories(parent);
            }

            try(BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)){
                writer.write("linha;status;resultado;pre_mRNA");
                writer.newLine();

                int line = 1;

                for(Analysis analysis : analyses){
                    String status = analysis.getResultType() == ResultType.CORRECT ? "OK" : "ERRO";
                    String result = getResultMessage(analysis.getResultType());
                    String preMrna = analysis.getResultType() == ResultType.CORRECT ? getValue(analysis.getPreMrna(), "NÃO GERADO") : "NÃO GERADO";

                    writer.write(String.format("%d;%s;%s;%s", line, status, result, preMrna));
                    writer.newLine();
                    line++;
                }

                System.out.println();
                System.out.println("Resultados exportados com sucesso para: " + path.toAbsolutePath());
            }
            catch(IOException e){
                System.out.println();
                System.out.println("Erro ao exportar os resultados:");
                System.out.println(e.getMessage());
            }            
        } catch (IOException e) {
            System.out.println();
            System.out.println("Erro ao criar diretório para exportação:");
            System.out.println(e.getMessage());
        }

    }  
}
