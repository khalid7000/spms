package com.rit.spms.repository;

import com.rit.spms.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByStrategyIdAndEntityTypeAndEntityIdOrderByCreatedAtAsc(
            Long strategyId, String entityType, Long entityId);
    List<Comment> findByStrategyIdOrderByCreatedAtAsc(Long strategyId);
}
