package com.rit.spms.service;

/**
 * AI drafting for the "Teaching Evaluations" achievement module: given the extracted text of an
 * employee's uploaded course-evaluation files, drafts a few strength bullet points and a few
 * areas-for-improvement bullet points, formatted as plain text ready to drop into an achievement's
 * "Details" field for the employee to review and edit. Unlike {@link PortfolioGoalSuggestionGenerator},
 * the result here is prose, not a list of records -- so no structured-output/JSON parsing is needed.
 */
public interface TeachingEvaluationDraftGenerator {

    String generateDraft(String extractedEvaluationText);

    String providerName();
}
