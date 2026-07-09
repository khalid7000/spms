package com.rit.spms.service;

/** Published when an employee sends a goal cycle back for more consideration; notifies the leader. */
public record GoalCycleSentBackEvent(Long cycleId, Long leaderId, Long employeeId) {
}
