package com.rit.spms.repository;

import com.rit.spms.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByStrategyIdOrderByCreatedAtDesc(Long strategyId, Pageable pageable);
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Modifying
    @Query("UPDATE AuditLog a SET a.strategy = null WHERE a.strategy.id = :strategyId")
    void clearStrategyReferences(@Param("strategyId") Long strategyId);
}
