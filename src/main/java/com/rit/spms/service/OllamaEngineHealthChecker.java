package com.rit.spms.service;

import com.rit.spms.config.AiProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * {@link AiEngineHealthChecker} backed by a local Ollama server -- default provider (app.ai.provider
 * unset or "ollama"), same config as every other Ollama-backed bean in this codebase.
 *
 * <p>Uses its own short-timeout RestClient rather than reusing a generation client's -- a health
 * probe must fail fast (seconds), not wait the hours a real generation call is allowed to take on
 * slow local hardware (see {@link AiProperties.Ollama}'s connectTimeoutSeconds/readTimeoutMinutes).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaEngineHealthChecker implements AiEngineHealthChecker {

    private final AiProperties aiProperties;
    private RestClient restClient;

    @PostConstruct
    private void init() {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(aiProperties.getOllama().getHealthCheckTimeoutSeconds()))
                .build());
        factory.setReadTimeout(Duration.ofSeconds(aiProperties.getOllama().getHealthCheckTimeoutSeconds()));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    // GET /api/tags is Ollama's own lightweight "is the server up and serving the API" endpoint --
    // unlike /api/generate, it doesn't invoke the model at all, so it responds in milliseconds even
    // on modest hardware and never risks tying up whatever's currently mid-generation.
    @Override
    public boolean isResponding() {
        try {
            restClient.get()
                    .uri(aiProperties.getOllama().getBaseUrl() + "/api/tags")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("AI engine health check failed against {}: {}",
                    aiProperties.getOllama().getBaseUrl(), e.getMessage());
            return false;
        }
    }

    @Override
    public String providerName() {
        return "ollama:" + aiProperties.getOllama().getModel();
    }
}
