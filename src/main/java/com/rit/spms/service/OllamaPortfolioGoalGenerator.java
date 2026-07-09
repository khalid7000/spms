package com.rit.spms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.config.AiProperties;
import com.rit.spms.domain.PortfolioCategory;
import com.rit.spms.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Free, self-hosted {@link PortfolioGoalSuggestionGenerator} backed by a local Ollama server.
 * Default provider (app.ai.provider unset or "ollama"), same config as the SWOT module's
 * {@link OllamaAreaGoalGenerator}. Ollama's {@code format: "json"} only guarantees syntactically
 * valid JSON, not a specific schema, so the exact shape is spelled out in the prompt and parsed
 * manually (unlike Claude's native structured-output mode).
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaPortfolioGoalGenerator implements PortfolioGoalSuggestionGenerator {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    private record OllamaGenerateRequest(String model, String prompt, String format, boolean stream) {
    }

    private record OllamaGenerateResponse(String response) {
    }

    @Override
    public List<SuggestedGoalDto> generateGoalSuggestions(String leaderStrengths, String leaderWeaknesses,
                                                           List<PortfolioCategory> availableCategories) {
        String prompt = PortfolioAiPrompts.buildGoalSuggestionPromptWithJsonShape(leaderStrengths, leaderWeaknesses, availableCategories);
        try {
            OllamaGenerateResponse resp = restClient.post()
                    .uri(aiProperties.getOllama().getBaseUrl() + "/api/generate")
                    .body(new OllamaGenerateRequest(aiProperties.getOllama().getModel(), prompt, "json", false))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (resp == null || resp.response() == null || resp.response().isBlank()) {
                throw new AiUnavailableException("Ollama returned an empty response");
            }
            SuggestedGoalListDto parsed = objectMapper.readValue(resp.response(), SuggestedGoalListDto.class);
            return parsed.goals();
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate AI suggestions via Ollama ("
                    + aiProperties.getOllama().getBaseUrl() + "): " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return "ollama:" + aiProperties.getOllama().getModel();
    }
}
