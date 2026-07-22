package com.rit.spms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rit.spms.config.AiProperties;
import com.rit.spms.domain.enums.VsmNodeType;
import com.rit.spms.exception.AiUnavailableException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Free, self-hosted {@link VsmDraftGenerator} backed by a local Ollama server. Default provider
 * (app.ai.provider unset or "ollama"). This call runs fully in the background (see
 * VsmDraftGenerationService#generateDraftAsync) rather than blocking a leader in the UI, so --
 * same reasoning as {@link OllamaTeachingEvaluationDraftGenerator} -- it builds its own client
 * with the generous {@code app.ai.ollama.readTimeoutMinutes} read timeout rather than an
 * unconfigured default one, letting a slow local model actually finish instead of timing out.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaVsmDraftGenerator implements VsmDraftGenerator {

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
        factory.setReadTimeout(Duration.ofMinutes(aiProperties.getOllama().getReadTimeoutMinutes()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Override
    public SuggestedMapDto generateDraft(String processDescription, List<VsmNodeType> allowedNodeTypes) {
        String prompt = VsmAiPrompts.buildDraftPromptWithJsonShape(processDescription, allowedNodeTypes);
        try {
            OllamaGenerateResponse resp = restClient.post()
                    .uri(aiProperties.getOllama().getBaseUrl() + "/api/generate")
                    .body(new OllamaGenerateRequest(aiProperties.getOllama().getModel(), prompt, "json", false))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (resp == null || resp.response() == null || resp.response().isBlank()) {
                throw new AiUnavailableException("Ollama returned an empty response");
            }
            return objectMapper.readValue(resp.response(), SuggestedMapDto.class);
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate a Value Stream Map draft via Ollama ("
                    + aiProperties.getOllama().getBaseUrl() + "): " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return "ollama:" + aiProperties.getOllama().getModel();
    }
}
