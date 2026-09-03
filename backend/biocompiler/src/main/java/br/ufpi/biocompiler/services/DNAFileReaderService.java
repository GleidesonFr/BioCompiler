package br.ufpi.biocompiler.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DNAFileReaderService {

    public List<String> readSequences(MultipartFile file) throws IOException {
        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo não pode ser vazio");
        }

        return readSequences(file.getInputStream());
    }
    
    public List<String> readSequences(InputStream file) throws IOException{
        if(file == null || file.available() == 0) {
            throw new IllegalArgumentException("O arquivo não pode ser vazio");
        }

        try(BufferedReader reader = new BufferedReader(
            new InputStreamReader(file,StandardCharsets.UTF_8))){
            
            List<String> sequences = new ArrayList<String>();

            for(String line : reader.lines().toList()){
                String sequence = line.trim();
                sequence = sequence.replace("\uFEFF", "");

                if(sequence.equalsIgnoreCase("entrada") ||
                sequence.equalsIgnoreCase(".entrada")){
                    continue;
                }else{
                    sequences.add(sequence); 
                }
            }
            
            if(sequences.isEmpty()){
                throw new IllegalArgumentException("O arquivo não contém nenhuma sequência");
            }

            return sequences;
        }
    }
}
