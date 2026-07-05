package com.rit.spms.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class StrategyIncompleteException extends RuntimeException {
    private final List<Long> areasWithoutGoals;
    private final List<Long> goalsWithoutObjectives;
    private final List<Long> objectivesWithoutInitiatives;
    private final List<Long> initiativesWithoutMeasurements;

    public StrategyIncompleteException(List<Long> areasWithoutGoals,
                                       List<Long> goalsWithoutObjectives,
                                       List<Long> objectivesWithoutInitiatives,
                                       List<Long> initiativesWithoutMeasurements) {
        super("Strategy cannot advance from CREATION: some items are incomplete");
        this.areasWithoutGoals = areasWithoutGoals;
        this.goalsWithoutObjectives = goalsWithoutObjectives;
        this.objectivesWithoutInitiatives = objectivesWithoutInitiatives;
        this.initiativesWithoutMeasurements = initiativesWithoutMeasurements;
    }
}
