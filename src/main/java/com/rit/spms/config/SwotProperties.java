package com.rit.spms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.swot")
@Getter
@Setter
public class SwotProperties {
    private int minWordsPerQuadrant = 3;
    private int maxWordsPerQuadrant = 15;
    private int voteRankCount = 3;
    private List<Integer> voteRankWeights = List.of(3, 2, 1);
    private int topWordsPerQuadrantForAi = 5;
    private int synonymSuggestionCount = 6;
}
