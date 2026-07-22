package com.rit.spms.service;

/** Published when a task's owner (or the map's author/admin) adds a collaborator to a task;
 *  notifies the newly-added employee. */
public record VsmTaskAssignedEvent(Long taskId, Long employeeId) {
}
