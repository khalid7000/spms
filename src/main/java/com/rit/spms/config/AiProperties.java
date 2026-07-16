package com.rit.spms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Selects and configures the pluggable AI provider used for SWOT area/goal generation. */
@Component
@ConfigurationProperties(prefix = "app.ai")
@Getter
@Setter
public class AiProperties {

    /** "ollama" (default, free/local) or "claude". */
    private String provider = "ollama";

    private final Ollama ollama = new Ollama();

    @Getter
    @Setter
    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String model = "llama3.1";

        // Ollama defaults to a small context window (often 2048-4096 tokens) unless a request
        // explicitly overrides it via options.num_ctx -- silently truncating anything beyond that,
        // with no error. Prompts built from long inputs (e.g. a whole PDF of several course
        // evaluations for TeachingEvaluationDraftGenerator) can easily exceed the default, so every
        // Ollama call in this codebase should pass this value rather than relying on the model's
        // built-in default.
        private int numCtx = 16384;

        // Generation itself already runs fully in the background (the employee can close the page
        // and come back later -- see TeachingEvaluationSessionService.generateDraftAsync), so there's
        // no reason for the HTTP call to Ollama to give up early: a large num_ctx on modest/CPU-only
        // local hardware can genuinely take a very long time. Read timeout defaults to a generous
        // 6 hours; raise it further here if a real run still times out before finishing.
        private int connectTimeoutSeconds = 10;
        private int readTimeoutMinutes = 360;
    }
}
