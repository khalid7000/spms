package com.rit.spms.service;

import com.rit.spms.domain.PortfolioCategory;

import java.util.List;

/** Shared prompt construction for {@link PortfolioGoalSuggestionGenerator} implementations. */
final class PortfolioAiPrompts {

    private PortfolioAiPrompts() {
    }

    static String buildGoalSuggestionPrompt(String leaderStrengths, String leaderWeaknesses,
                                             List<PortfolioCategory> availableCategories) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are helping a manager set annual improvement goals for a direct report as part of ")
                .append("a performance evaluation cycle.\n\n");
        sb.append("The employee is evaluated against these categories:\n");
        for (PortfolioCategory category : availableCategories) {
            sb.append("  - ").append(category.getCategoryName());
            if (category.getDescription() != null && !category.getDescription().isBlank()) {
                sb.append(": ").append(category.getDescription());
            }
            sb.append("\n");
        }
        sb.append("\nManager's notes on the employee's strengths:\n")
                .append(leaderStrengths != null && !leaderStrengths.isBlank() ? leaderStrengths : "(none provided)")
                .append("\n\nManager's notes on areas for improvement:\n")
                .append(leaderWeaknesses != null && !leaderWeaknesses.isBlank() ? leaderWeaknesses : "(none provided)")
                .append("\n\nBased on this, propose 2 to 5 concrete, measurable improvement goals for the coming ")
                .append("year. Each goal must target exactly one of the categories listed above (use the category ")
                .append("name exactly as written), and include a short title, a one-to-two sentence description, ")
                .append("and a one-sentence rationale tying it back to the manager's notes. Also draft a 3-level ")
                .append("rubric for judging the employee's achievement against the goal at year-end: a sentence or ")
                .append("two describing what \"unsatisfactory\", \"meets expectations\", and \"exceeds expectations\" ")
                .append("performance would each look like for that specific goal -- the manager can edit these before ")
                .append("finalizing.");
        return sb.toString();
    }

    /** Same content as above, plus an explicit JSON-shape instruction for models without native structured output. */
    static String buildGoalSuggestionPromptWithJsonShape(String leaderStrengths, String leaderWeaknesses,
                                                          List<PortfolioCategory> availableCategories) {
        return buildGoalSuggestionPrompt(leaderStrengths, leaderWeaknesses, availableCategories)
                + "\n\nRespond with ONLY a single JSON object (no markdown, no code fences, no commentary) "
                + "matching exactly this shape:\n"
                + "{\"goals\": [{\"categoryName\": \"string\", \"title\": \"string\", \"description\": \"string\", "
                + "\"rationale\": \"string\", \"rubricUnsatisfactory\": \"string\", \"rubricMeetsExpectations\": \"string\", "
                + "\"rubricExceedsExpectations\": \"string\"}]}";
    }
}
