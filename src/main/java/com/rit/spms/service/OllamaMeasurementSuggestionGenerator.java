package com.rit.spms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.config.AiProperties;
import com.rit.spms.exception.AiUnavailableException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Free, self-hosted {@link MeasurementSuggestionGenerator} backed by a local Ollama server.
 * Default provider (app.ai.provider unset or "ollama"). Unlike {@link OllamaAreaGoalGenerator}
 * (which uses an unconfigured default client because its generation runs in the background),
 * this call blocks a user waiting in the Add Initiative modal, so it builds its own short-timeout
 * client the same way {@link OllamaEngineHealthChecker} does -- see
 * {@code app.ai.ollama.measurementSuggestionTimeoutSeconds}.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaMeasurementSuggestionGenerator implements MeasurementSuggestionGenerator {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestClient restClient;

    private record OllamaGenerateRequest(String model, String prompt, String format, boolean stream) {
    }

    private record OllamaGenerateResponse(String response) {
    }

    @PostConstruct
    private void init() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(aiProperties.getOllama().getConnectTimeoutSeconds()))
                .build());
        factory.setReadTimeout(Duration.ofSeconds(aiProperties.getOllama().getMeasurementSuggestionTimeoutSeconds()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public SuggestedMeasurementDto suggestMeasurement(String objectiveTitle, String objectiveDescription,
                                                       String initiativeTitle, String initiativeDescription) {
        String prompt = MeasurementAiPrompts.buildMeasurementPromptWithJsonShape(
                objectiveTitle, objectiveDescription, initiativeTitle, initiativeDescription);
        try {
            OllamaGenerateResponse resp = restClient.post()
                    .uri(aiProperties.getOllama().getBaseUrl() + "/api/generate")
                    .body(new OllamaGenerateRequest(aiProperties.getOllama().getModel(), prompt, "json", false))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (resp == null || resp.response() == null || resp.response().isBlank()) {
                throw new AiUnavailableException("Ollama returned an empty response");
            }
            return objectMapper.readValue(resp.response(), SuggestedMeasurementDto.class);
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate a measurement suggestion via Ollama ("
                    + aiProperties.getOllama().getBaseUrl() + "): " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return "ollama:" + aiProperties.getOllama().getModel();
    }
}
