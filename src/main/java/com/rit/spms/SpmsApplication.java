package com.rit.spms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application entry point. @EnableAsync backs the SWOT workflow's background AI
 * suggestion generation (SwotSuggestionService.generateSuggestions) so a slow
 * model call never blocks the HTTP request that triggered it — see that class
 * for why this matters (local LLMs can take a minute or more per call).
 * @EnableScheduling backs AiEngineHealthCheckService's periodic Ollama ping.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SpmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpmsApplication.class, args);
    }
}
