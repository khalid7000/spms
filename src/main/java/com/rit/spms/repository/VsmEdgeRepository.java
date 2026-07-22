package com.rit.spms.repository;

import com.rit.spms.domain.VsmEdge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VsmEdgeRepository extends JpaRepository<VsmEdge, Long> {
    List<VsmEdge> findByVsmMapId(Long vsmMapId);
    void deleteByVsmMapId(Long vsmMapId);
}
