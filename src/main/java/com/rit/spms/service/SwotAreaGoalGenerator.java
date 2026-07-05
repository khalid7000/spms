package com.rit.spms.service;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotQuadrantResult;
import com.rit.spms.domain.enums.SwotQuadrant;

import java.util.List;
import java.util.Map;

/**
 * Pluggable AI provider for turning top-voted SWOT results into suggested focus areas/goals.
 * Selected at runtime via {@code app.ai.provider} (application.yml) — see {@link ClaudeAreaGoalGenerator}
 * and {@link OllamaAreaGoalGenerator} for the two implementations.
 */
public interface SwotAreaGoalGenerator {

    record SuggestedGoalDto(String title, String description) {
    }

    record SuggestedAreaDto(String name, String rationale, List<SuggestedGoalDto> goals) {
    }

    record SuggestedAreaListDto(List<SuggestedAreaDto> areas) {
    }

    List<SuggestedAreaDto> generateAreasAndGoals(Strategy strategy, Map<SwotQuadrant, List<SwotQuadrantResult>> topWordsByQuadrant);

    /** Recorded on each persisted SwotSuggestion for audit (e.g. "claude-opus-4-8", "ollama:llama3.1"). */
    String providerName();
}
