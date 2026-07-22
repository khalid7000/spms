package com.rit.spms.service;

/** Published when a pulled improvement task is marked done; notifies the task's map's author. */
public record VsmTaskCompletedEvent(Long taskId) {
}
