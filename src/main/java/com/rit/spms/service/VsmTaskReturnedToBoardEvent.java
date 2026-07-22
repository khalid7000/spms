package com.rit.spms.service;

/** Published when the map's author/admin pulls a PULLED or IN_PROGRESS task back off its assignee
 *  and returns it to the board as AVAILABLE. Carries {@code previousPullerId} separately since the
 *  task's own {@code pulledBy} has already been cleared by the time this fires. */
public record VsmTaskReturnedToBoardEvent(Long taskId, Long previousPullerId) {
}
