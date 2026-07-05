package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.SwotPhase;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SwotStatusResponse {
    private boolean sessionStarted;
    private SwotPhase phase;

    // Only meaningful while phase == GENERATING_SUGGESTIONS: when non-null, the UI can compute
    // elapsed time client-side ("submitted at X, Y elapsed") instead of just saying "still waiting".
    private LocalDateTime generationRequestedAt;
    // Non-null only if the last generation attempt threw — lets the UI show a real failure reason
    // instead of leaving the owner to guess whether "Retry Generation" means "it's stuck" or "it broke".
    private String generationFailureReason;

    // Named without an "is" prefix on purpose: Jackson serializes a Lombok boolean getter
    // isOwner() as JSON property "owner" (strips "is" via standard bean-introspection rules)
    // regardless of the Java field's own name, so "isOwner" would just be a confusing field
    // name for the same wire output. Matches what the JSON actually looks like.
    private boolean owner;
    private boolean participant;

    private long totalParticipants;
    private long submittedCount;
    private long votedCount;
    private long nonOwnerParticipants;
    private long reviewedCount;

    private boolean mySwotSubmitted;
    private boolean myVoteSubmitted;
    private boolean myReviewSubmitted;
}
