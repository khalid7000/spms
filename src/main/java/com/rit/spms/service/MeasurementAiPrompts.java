package com.rit.spms.service;

/** Shared prompt construction for {@link MeasurementSuggestionGenerator} implementations. */
final class MeasurementAiPrompts {

    private MeasurementAiPrompts() {
    }

    static String buildMeasurementPrompt(String objectiveTitle, String objectiveDescription,
                                          String initiativeTitle, String initiativeDescription) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are helping a university/department define a measurable KPI for one initiative ")
                .append("in a strategic plan.\n\n");
        sb.append("Objective: ").append(objectiveTitle).append("\n");
        if (objectiveDescription != null && !objectiveDescription.isBlank()) {
            sb.append("Objective description: ").append(objectiveDescription).append("\n");
        }
        sb.append("Initiative: ").append(initiativeTitle).append("\n");
        if (initiativeDescription != null && !initiativeDescription.isBlank()) {
            sb.append("Initiative description: ").append(initiativeDescription).append("\n");
        }
        sb.append("\nPropose exactly ONE specific, measurable KPI for this initiative: a short ")
                .append("description of what to count or measure, a short unit symbol (e.g. \"#\", \"%\", ")
                .append("\"hrs\"), and a single numeric target value (the minimum/target to reach). ")
                .append("Keep the description concise (under 20 words) and phrased as something a ")
                .append("planning team would track achievements against.");
        return sb.toString();
    }

    /** Same content as above, plus an explicit JSON-shape instruction for models without native structured output. */
    static String buildMeasurementPromptWithJsonShape(String objectiveTitle, String objectiveDescription,
                                                       String initiativeTitle, String initiativeDescription) {
        return buildMeasurementPrompt(objectiveTitle, objectiveDescription, initiativeTitle, initiativeDescription)
                + "\n\nRespond with ONLY a single JSON object (no markdown, no code fences, no commentary) "
                + "matching exactly this shape:\n"
                + "{\"description\": \"string\", \"unit\": \"string\", \"targetValue\": number}";
    }
}
