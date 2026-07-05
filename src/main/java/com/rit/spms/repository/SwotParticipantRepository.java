package com.rit.spms.repository;

import com.rit.spms.domain.SwotParticipant;
import com.rit.spms.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwotParticipantRepository extends JpaRepository<SwotParticipant, Long> {
    List<SwotParticipant> findBySwotSessionId(Long swotSessionId);
    Optional<SwotParticipant> findBySwotSessionIdAndUserId(Long swotSessionId, Long userId);

    long countBySwotSessionId(Long swotSessionId);
    long countBySwotSessionIdAndSwotSubmittedAtIsNotNull(Long swotSessionId);
    long countBySwotSessionIdAndVoteSubmittedAtIsNotNull(Long swotSessionId);
    long countBySwotSessionIdAndRoleAtInviteNot(Long swotSessionId, RoleType roleAtInvite);
    long countBySwotSessionIdAndRoleAtInviteNotAndReviewSubmittedAtIsNotNull(Long swotSessionId, RoleType roleAtInvite);

    @Query("select p from SwotParticipant p join fetch p.swotSession s join fetch s.strategy where p.user.id = :userId")
    List<SwotParticipant> findByUserIdWithSession(Long userId);
}
