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
    }
}
