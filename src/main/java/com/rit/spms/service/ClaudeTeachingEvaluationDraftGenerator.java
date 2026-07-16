package com.rit.spms.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.rit.spms.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Claude API-backed {@link TeachingEvaluationDraftGenerator}. Active only when
 * {@code app.ai.provider=claude} (application.yml); requires ANTHROPIC_API_KEY in the process
 * environment. Unlike {@link ClaudePortfolioGoalGenerator}, this doesn't use
 * {@code StructuredMessageCreateParams} -- the result is prose for a "Details" field, not a list
 * of records -- so it's a plain {@link MessageCreateParams} call, reading the raw text block back.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "claude")
public class ClaudeTeachingEvaluationDraftGenerator implements TeachingEvaluationDraftGenerator {

    private static final String MODEL_ID = "claude-opus-4-8";

    private final AnthropicClient anthropicClient;

    @Override
    public String generateDraft(String extractedEvaluationText) {
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.of(MODEL_ID))
                    .maxTokens(2000L)
                    .addUserMessage(TeachingEvaluationAiPrompts.buildDraftPrompt(extractedEvaluationText))
                    .build();

            Message message = anthropicClient.messages().create(params);
            return message.content().stream()
                    .flatMap(cb -> cb.text().stream())
                    .map(com.anthropic.models.messages.TextBlock::text)
                    .findFirst()
                    .map(String::trim)
                    .orElseThrow(() -> new AiUnavailableException("Claude returned no draft text"));
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate a draft via Claude: " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return MODEL_ID;
    }
}
