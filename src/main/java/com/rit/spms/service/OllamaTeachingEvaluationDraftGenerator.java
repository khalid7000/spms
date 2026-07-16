package com.rit.spms.service;

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
 * Free, self-hosted {@link TeachingEvaluationDraftGenerator} backed by a local Ollama server --
 * default provider (app.ai.provider unset or "ollama"), same config as every other Ollama
 * generator in this codebase. The result here is plain prose (not JSON), so -- unlike
 * {@link OllamaPortfolioGoalGenerator} -- no {@code format: "json"} or parsing is needed; the raw
 * model response text is used as-is.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaTeachingEvaluationDraftGenerator implements TeachingEvaluationDraftGenerator {

    private final AiProperties aiProperties;
    private RestClient restClient;

    // Built in @PostConstruct (not a field initializer) so it can read the constructor-injected
    // aiProperties' configured timeouts. RestClient.create()'s default request factory relies on
    // whatever HTTP client Spring auto-detects, which times out this call well before a large
    // num_ctx generation can finish on modest/CPU-only local hardware -- this call already runs
    // fully in the background (see TeachingEvaluationSessionService.generateDraftAsync), so a very
    // generous read timeout costs nothing and lets a slow local model actually finish.
    @PostConstruct
    private void init() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(aiProperties.getOllama().getConnectTimeoutSeconds()))
                .build());
        factory.setReadTimeout(Duration.ofMinutes(aiProperties.getOllama().getReadTimeoutMinutes()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    private record OllamaOptions(int num_ctx) {
    }

    // Course-evaluation text extracted from a whole PDF (potentially many courses' worth) can
    // easily run to several thousand tokens -- options.num_ctx must be set explicitly, or Ollama
    // silently truncates to its own small default context window and the model only ever sees the
    // first course or two, however many were actually uploaded.
    private record OllamaGenerateRequest(String model, String prompt, boolean stream, OllamaOptions options) {
    }

    private record OllamaGenerateResponse(String response) {
    }

    @Override
    public String generateDraft(String extractedEvaluationText) {
        String prompt = TeachingEvaluationAiPrompts.buildDraftPrompt(extractedEvaluationText);
        try {
            OllamaGenerateResponse resp = restClient.post()
                    .uri(aiProperties.getOllama().getBaseUrl() + "/api/generate")
                    .body(new OllamaGenerateRequest(aiProperties.getOllama().getModel(), prompt, false,
                            new OllamaOptions(aiProperties.getOllama().getNumCtx())))
                    .retrieve()
                    .body(OllamaGenerateResponse.class);

            if (resp == null || resp.response() == null || resp.response().isBlank()) {
                throw new AiUnavailableException("Ollama returned an empty response");
            }
            return resp.response().trim();
        } catch (AiUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new AiUnavailableException("Unable to generate a draft via Ollama ("
                    + aiProperties.getOllama().getBaseUrl() + "): " + e.getMessage(), e);
        }
    }

    @Override
    public String providerName() {
        return "ollama:" + aiProperties.getOllama().getModel();
    }
}
