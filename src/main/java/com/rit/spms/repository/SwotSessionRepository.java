package com.rit.spms.repository;

import com.rit.spms.domain.SwotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SwotSessionRepository extends JpaRepository<SwotSession, Long> {
    Optional<SwotSession> findByStrategyId(Long strategyId);
    boolean existsByStrategyId(Long strategyId);
}
