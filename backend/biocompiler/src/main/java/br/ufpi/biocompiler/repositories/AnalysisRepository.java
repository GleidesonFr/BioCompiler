package br.ufpi.biocompiler.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.ufpi.biocompiler.models.Analysis;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    Page<Analysis> findAllBySessionIdOrderByAnalysisDateDesc(UUID sessionId, Pageable pageable);

    void deleteAllBySessionId(UUID sessionId);

    @Query(
        """
           SELECT a.resultType, count(a)
           from Analysis a
           WHERE a.sessionId = :sessionId
           GROUP BY a.resultType     
        """
    )
    List<Object[]> countAnalysesByResultType(@Param("sessionId") UUID sessionId);
}
