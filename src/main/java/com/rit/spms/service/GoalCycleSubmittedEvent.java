package com.rit.spms.service;

/** Published when a leader (re)submits an employee goal cycle for the employee's review; notifies the employee. */
public record GoalCycleSubmittedEvent(Long cycleId, Long employeeId, Long leaderId) {
}
