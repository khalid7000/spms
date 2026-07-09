package com.rit.spms.service;

/** Published when a user is assigned a role (OWNER/EDITOR/COMMENTER/VIEWER) on a strategy; notifies them. */
public record StrategyMemberAddedEvent(Long strategyId, Long userId, String role) {
}
