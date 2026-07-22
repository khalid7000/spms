package com.rit.spms.repository;

import com.rit.spms.domain.VsmNodeMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VsmNodeMetricRepository extends JpaRepository<VsmNodeMetric, Long> {
}
