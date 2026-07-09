package com.rit.spms.service;

/** Published once per participant when a strategy's SWOT analysis is started; notifies them they're invited. */
public record SwotInviteEvent(Long strategyId, Long userId) {
}
