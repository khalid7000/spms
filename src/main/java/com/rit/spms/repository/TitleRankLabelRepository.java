package com.rit.spms.repository;

import com.rit.spms.domain.TitleRankLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TitleRankLabelRepository extends JpaRepository<TitleRankLabel, Long> {
    List<TitleRankLabel> findByTitleIdOrderByRank(Long titleId);
    Optional<TitleRankLabel> findByTitleIdAndRank(Long titleId, Integer rank);
}
