package com.rit.spms.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.rit.spms.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Claude API-backed {@link MeasurementSuggestionGenerator}. Active only when
 * {@code app.ai.provider=claude} (application.yml); requires ANTHROPIC_API_KEY in the process
 * environment. Claude responds quickly enough for this synchronous call that no dedicated
 * timeout handling is needed beyond the shared {@link AnthropicClient} bean's own defaults.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "claude")
public class ClaudeMeasurementSuggestionGenerator implements MeasurementSuggestionGenerator {

    private static final String MODEL_ID = "claude-opus-4-8";

    private final AnthropicClient anthropicClient;

    @Override
    public SuggestedMeasurementDto suggestMeasurement(String objectiveTitle, String objectiveDescription,
                                                       String initiativeTitle, String initiativeDescription) {
        try {
            StructuredMessageCreateParams<SuggestedMeasurementDto> params = MessageCreateParams.builder()
                    .model(Model.of(MODEL_ID))
                    .maxTokens(500L)
                    .outputConfig(SuggestedMeasurementDto.class)
                    .addUserMessage(MeasurementAiPrompts.buildMeasurementPrompt(
                            objectiveTitle, objectiveDescription, initiativeTitle, initiativeDescription))
                    .build();

            return anthropicClient.messages().create(params).content().stream()
                    .flatMap(cb -> cb.text().stream())
                    .map(t -> t.text())
                    .findFirst()
                    .orElseThrow(() -> new AiUnavailableException("Claude returned no suggestion"));
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate a measurement suggestion via Claude: " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return MODEL_ID;
    }
}
