package com.rit.spms.dto.response;

import com.rit.spms.domain.enums.VsmMapState;
import com.rit.spms.domain.enums.VsmScopeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Full map detail: metadata plus every node/edge, for the canvas page. Built by VsmMapService
 *  rather than a static from() since it joins several repositories' worth of data (nodes, edges). */
@Data
@Builder
public class VsmMapResponse {
    private Long id;
    private VsmScopeType scopeType;
    private Long departmentId;
    private String departmentName;
    private Long orgGroupId;
    private String orgGroupName;
    private String title;
    private String description;
    private VsmMapState state;

    /** Whether the *current viewer* (not just the map's author) may edit this canvas/state --
     *  server-resolved via PermissionService#canEditVsmMap so the frontend never has to duplicate
     *  the author-or-admin rule. A same-department viewer who isn't the author gets a read-only
     *  canvas instead of edit controls that would 403 if used. */
    private boolean canEdit;

    private LocalDateTime updatedAt;
    private List<VsmNodeResponse> nodes;
    private List<VsmEdgeResponse> edges;

    /** Only populated on the response to a canvas save -- maps each new node's client-side tempId
     *  to the real id the server assigned it, so the frontend can reconcile its local React Flow state. */
    private Map<String, Long> tempIdMapping;

    // AI draft generation status -- the frontend derives "generating"/"failed"/"done" purely from
    // these three fields (no separate status enum), same convention as the goal-cycle/teaching-
    // evaluation AI flows. draftProcessDescription is echoed back so a retry-after-failure needs no
    // user re-entry.
    private LocalDateTime generationRequestedAt;
    private LocalDateTime generatedAt;
    private String generationFailureReason;
    private String draftProcessDescription;
}
