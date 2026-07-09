package com.rit.spms.service;

import com.rit.spms.domain.PortfolioCategory;

import java.util.List;

/**
 * Pluggable AI provider for turning a leader's strength/weakness notes into suggested improvement
 * goals for an employee. Selected at runtime via {@code app.ai.provider} (application.yml) --
 * see {@link ClaudePortfolioGoalGenerator} and {@link OllamaPortfolioGoalGenerator}, the same
 * selection mechanism used by {@link SwotAreaGoalGenerator} for the SWOT module.
 */
public interface PortfolioGoalSuggestionGenerator {

    record SuggestedGoalDto(String categoryName, String title, String description, String rationale,
                             String rubricUnsatisfactory, String rubricMeetsExpectations, String rubricExceedsExpectations) {
    }

    record SuggestedGoalListDto(List<SuggestedGoalDto> goals) {
    }

    List<SuggestedGoalDto> generateGoalSuggestions(String leaderStrengths, String leaderWeaknesses,
                                                    List<PortfolioCategory> availableCategories);

    /** Recorded on each persisted EmployeeGoalSuggestion for audit (e.g. "claude-opus-4-8", "ollama:llama3.1"). */
    String providerName();
}
