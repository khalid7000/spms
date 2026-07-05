package com.rit.spms.repository;

import com.rit.spms.domain.VisionArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisionAreaRepository extends JpaRepository<VisionArea, Long> {
    List<VisionArea> findByStrategyIdOrderBySortOrder(Long strategyId);
    boolean existsByStrategyId(Long strategyId);
}
