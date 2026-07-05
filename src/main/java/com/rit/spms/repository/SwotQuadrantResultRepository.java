package com.rit.spms.repository;

import com.rit.spms.domain.SwotQuadrantResult;
import com.rit.spms.domain.enums.SwotQuadrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwotQuadrantResultRepository extends JpaRepository<SwotQuadrantResult, Long> {
    List<SwotQuadrantResult> findBySwotSessionIdOrderByQuadrantAscRankPositionAsc(Long swotSessionId);
    List<SwotQuadrantResult> findBySwotSessionIdAndQuadrantOrderByRankPositionAsc(Long swotSessionId, SwotQuadrant quadrant);
    void deleteBySwotSessionId(Long swotSessionId);
}
