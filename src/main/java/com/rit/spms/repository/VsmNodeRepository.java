package com.rit.spms.repository;

import com.rit.spms.domain.VsmNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VsmNodeRepository extends JpaRepository<VsmNode, Long> {
    List<VsmNode> findByVsmMapIdOrderById(Long vsmMapId);

    /** Bulk lookup across every map a user can see -- powers VsmAnalyticsService's cross-map
     *  bottleneck rollup (Phase 6) in one query instead of one per map. */
    List<VsmNode> findByVsmMapIdIn(Collection<Long> vsmMapIds);
}
