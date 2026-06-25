package com.rit.spms.repository;

import com.rit.spms.domain.CommentRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface CommentReadRepository extends JpaRepository<CommentRead, Long> {

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT cr.comment.id FROM CommentRead cr WHERE cr.user.id = :userId AND cr.comment.id IN :commentIds")
    Set<Long> findReadCommentIds(@Param("userId") Long userId, @Param("commentIds") java.util.Collection<Long> commentIds);

    @Modifying
    @Query("DELETE FROM CommentRead cr WHERE cr.comment.strategy.id = :strategyId AND cr.user.id = :userId AND cr.comment.entityType = :entityType AND cr.comment.entityId = :entityId")
    void markAllReadForElement(@Param("strategyId") Long strategyId, @Param("userId") Long userId,
                               @Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.strategy.id = :strategyId AND c.id NOT IN (SELECT cr.comment.id FROM CommentRead cr WHERE cr.user.id = :userId)")
    long countUnreadForStrategy(@Param("strategyId") Long strategyId, @Param("userId") Long userId);
}
