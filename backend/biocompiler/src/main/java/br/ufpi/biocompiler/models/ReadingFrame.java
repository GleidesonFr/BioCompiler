package br.ufpi.biocompiler.models;

import lombok.Getter;

@Getter
public enum ReadingFrame {
    FRAME_0(0),
    FRAME_1(1),
    FRAME_2(2);

    private final int frame;

    ReadingFrame(int frame) {
        this.frame = frame;
    }
}