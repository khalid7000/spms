package com.rit.spms.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Claude API-backed {@link PortfolioGoalSuggestionGenerator}. Active only when
 * {@code app.ai.provider=claude} (application.yml); requires ANTHROPIC_API_KEY in the process
 * environment. Mirrors {@link ClaudeAreaGoalGenerator}.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "claude")
public class ClaudePortfolioGoalGenerator implements PortfolioGoalSuggestionGenerator {

    private static final String MODEL_ID = "claude-opus-4-8";

    private final AnthropicClient anthropicClient;

    @Override
    public List<SuggestedGoalDto> generateGoalSuggestions(String leaderStrengths, String leaderWeaknesses,
                                                           List<PortfolioCategory> availableCategories) {
        try {
            StructuredMessageCreateParams<SuggestedGoalListDto> params = MessageCreateParams.builder()
                    .model(Model.of(MODEL_ID))
                    .maxTokens(4000L)
                    .outputConfig(SuggestedGoalListDto.class)
                    .addUserMessage(PortfolioAiPrompts.buildGoalSuggestionPrompt(leaderStrengths, leaderWeaknesses, availableCategories))
                    .build();

            return anthropicClient.messages().create(params).content().stream()
                    .flatMap(cb -> cb.text().stream())
                    .map(t -> t.text().goals())
                    .findFirst()
                    .orElseThrow(() -> new AiUnavailableException("Claude returned no suggestions"));
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate AI suggestions via Claude: " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return MODEL_ID;
    }
}
