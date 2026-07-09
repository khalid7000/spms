package com.rit.spms.service;

/** Published when an employee accepts a goal cycle (goals deployed for the year); notifies the leader. */
public record GoalCycleDeployedEvent(Long cycleId, Long leaderId, Long employeeId) {
}
