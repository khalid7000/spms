package com.rit.spms.service;

import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotQuadrantResult;
import com.rit.spms.domain.enums.SwotQuadrant;

import java.util.List;
import java.util.Map;

/** Shared prompt construction for {@link SwotAreaGoalGenerator} implementations. */
final class SwotAiPrompts {

    private SwotAiPrompts() {
    }

    static String buildAreaGoalPrompt(Strategy strategy, Map<SwotQuadrant, List<SwotQuadrantResult>> topWordsByQuadrant) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are helping a university/department produce a strategic plan.\n");
        sb.append("Strategy: ").append(strategy.getTitle()).append("\n");
        if (strategy.getDescription() != null && !strategy.getDescription().isBlank()) {
            sb.append("Description: ").append(strategy.getDescription()).append("\n");
        }
        sb.append("\nBelow are the top SWOT words the planning team ranked highest by weighted vote, ")
                .append("grouped by quadrant (highest-ranked first):\n\n");
        for (SwotQuadrant quadrant : SwotQuadrant.values()) {
            sb.append(quadrant).append(":\n");
            List<SwotQuadrantResult> words = topWordsByQuadrant.getOrDefault(quadrant, List.of());
            if (words.isEmpty()) {
                sb.append("  (none)\n");
            } else {
                for (SwotQuadrantResult r : words) {
                    sb.append("  - ").append(r.getDisplayWord())
                            .append(" (score ").append(r.getTotalScore()).append(")\n");
                }
            }
        }
        sb.append("\nBased on these SWOT results, propose 3 to 6 strategic focus areas the organization ")
                .append("should pursue over the planning cycle. For each area, propose 2 to 5 concrete goals. ")
                .append("Each area needs a short name, a one-to-two sentence rationale grounded in the SWOT ")
                .append("words above, and each goal needs a title and a one-sentence description.");
        return sb.toString();
    }

    /** Same content as above, plus an explicit JSON-shape instruction for models without native structured output. */
    static String buildAreaGoalPromptWithJsonShape(Strategy strategy, Map<SwotQuadrant, List<SwotQuadrantResult>> topWordsByQuadrant) {
        return buildAreaGoalPrompt(strategy, topWordsByQuadrant)
                + "\n\nRespond with ONLY a single JSON object (no markdown, no code fences, no commentary) "
                + "matching exactly this shape:\n"
                + "{\"areas\": [{\"name\": \"string\", \"rationale\": \"string\", "
                + "\"goals\": [{\"title\": \"string\", \"description\": \"string\"}]}]}";
    }
}
