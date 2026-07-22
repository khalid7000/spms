package com.rit.spms.service;

/** Published when a faculty member pulls an available improvement task off a Kanban board; notifies
 *  the task's map's author. */
public record VsmTaskPulledEvent(Long taskId) {
}
