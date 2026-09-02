package br.ufpi.biocompiler.errors;

public class FrameInvalidException extends RuntimeException{
    
    public FrameInvalidException(){
        super("Quadro de leitura inválido.");
    }
}
