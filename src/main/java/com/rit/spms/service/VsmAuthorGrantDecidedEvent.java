package com.rit.spms.service;

/** Published when a VSM author grant is approved or rejected; notifies the delegated employee. */
public record VsmAuthorGrantDecidedEvent(Long grantId) {
}
