package com.rit.spms.service;

import com.rit.spms.domain.enums.VsmEdgeType;
import com.rit.spms.domain.enums.VsmNodeType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pluggable AI provider for turning a leader's free-text process description into a draft Value
 * Stream Map (nodes + edges), letting them start from something rather than a blank canvas.
 * Selected at runtime via {@code app.ai.provider} (application.yml) -- same mechanism as {@link
 * SwotAreaGoalGenerator}/{@link MeasurementSuggestionGenerator}. Nothing from this call is
 * persisted: it's a preview the leader edits on the canvas before an explicit save (see
 * VsmMapService#saveCanvas) -- {@code tempId} on each suggested node is the same client-side-key
 * mechanism a brand-new manually-added node uses, so the canvas save path needs no special case for
 * AI-originated nodes.
 */
public interface VsmDraftGenerator {

    record SuggestedNodeDto(String tempId, VsmNodeType nodeType, String title, String description,
                             BigDecimal cycleTimeMinutes, BigDecimal completeAccuratePercent,
                             BigDecimal failRatePercent) {
    }

    record SuggestedEdgeDto(String sourceTempId, String targetTempId, VsmEdgeType edgeType, String label) {
    }

    record SuggestedMapDto(List<SuggestedNodeDto> nodes, List<SuggestedEdgeDto> edges) {
    }

    /**
     * @param allowedNodeTypes the node types this installation's enabled notation packs permit
     *                         (see VsmNotationPack) -- the prompt is restricted to these, but the
     *                         caller (VsmDraftGenerationService) still filters the result
     *                         defensively, since a model can still hallucinate outside the list.
     */
    SuggestedMapDto generateDraft(String processDescription, List<VsmNodeType> allowedNodeTypes);

    /** Recorded nowhere today (this draft isn't persisted) -- kept for parity with the other AI
     *  provider interfaces in case an audit trail is added later. */
    String providerName();
}
