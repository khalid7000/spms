package com.rit.spms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.config.AiProperties;
import com.rit.spms.domain.Strategy;
import com.rit.spms.domain.SwotQuadrantResult;
import com.rit.spms.domain.enums.SwotQuadrant;
import com.rit.spms.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Free, self-hosted {@link SwotAreaGoalGenerator} backed by a local Ollama server
 * (https://ollama.com — run {@code ollama pull <model>} once, then {@code ollama serve}).
 * Default provider (app.ai.provider unset or "ollama"); configure the model/endpoint under
 * app.ai.ollama in application.yml. Ollama's {@code format: "json"} only guarantees syntactically
 * valid JSON, not a specific schema, so the exact shape is spelled out in the prompt and parsed
 * manually (unlike Claude's native structured-output mode).
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaAreaGoalGenerator implements SwotAreaGoalGenerator {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    private record OllamaGenerateRequest(String model, String prompt, String format, boolean stream) {
    }

    private record OllamaGenerateResponse(String response) {
    }

    @Override
    public List<SuggestedAreaDto> generateAreasAndGoals(
            Strategy strategy, Map<SwotQuadrant, List<SwotQuadrantResult>> topWordsByQuadrant) {
        String prompt = SwotAiPrompts.buildAreaGoalPromptWithJsonShape(strategy, topWordsByQuadrant);
        try {
            OllamaGenerateResponse resp = restClient.post()
                    .uri(aiProperties.getOllama().getBaseUrl() + "/api/generate")
                    .body(new OllamaGenerateRequest(aiProperties.getOllama().getModel(), prompt, "json", false))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (resp == null || resp.response() == null || resp.response().isBlank()) {
                throw new AiUnavailableException("Ollama returned an empty response");
            }
            SuggestedAreaListDto parsed = objectMapper.readValue(resp.response(), SuggestedAreaListDto.class);
            return parsed.areas();
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
