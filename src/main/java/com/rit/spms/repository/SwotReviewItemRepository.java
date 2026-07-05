package com.rit.spms.repository;

import com.rit.spms.domain.SwotReviewItem;
import com.rit.spms.domain.enums.SwotReviewTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwotReviewItemRepository extends JpaRepository<SwotReviewItem, Long> {
    List<SwotReviewItem> findBySwotSessionIdAndIsOwnerFinal(Long swotSessionId, Boolean isOwnerFinal);
    List<SwotReviewItem> findBySwotSessionIdAndReviewerIdAndIsOwnerFinal(Long swotSessionId, Long reviewerId, Boolean isOwnerFinal);
    Optional<SwotReviewItem> findBySwotSessionIdAndReviewerIdAndTargetTypeAndTargetIdAndIsOwnerFinal(
            Long swotSessionId, Long reviewerId, SwotReviewTargetType targetType, Long targetId, Boolean isOwnerFinal);
}
