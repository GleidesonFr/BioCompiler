package br.ufpi.biocompiler.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = Analysis.TABLE_NAME)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Analysis {
    protected static final String TABLE_NAME = "analyses";

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "original_sequence", nullable = false, length = 10000)
    private String originalSequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_type", nullable = false)
    private ResultType resultType;

    @Column(name = "position_start")
    private Integer positionStart;

    @Column(name = "position_stop")
    private Integer positionStop;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_frame")
    private ReadingFrame readingFrame;

    @Column(name = "coding_region", length = 10000)
    private String codingRegion;

    @Column(name = "pre_mrna", length = 10000)
    private String preMrna;

    @Column(length = 1000)
    private String message;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate = LocalDateTime.now();

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;
}
