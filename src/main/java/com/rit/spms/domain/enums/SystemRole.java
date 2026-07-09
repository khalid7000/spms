package com.rit.spms.domain.enums;

/**
 * System-wide roles a user can hold in addition to the implicit base "Employee" capability
 * (owning/editing/viewing strategies, having a portfolio) every AppUser already has.
 * A user can hold any combination of these at once.
 */
public enum SystemRole {
    ADMIN,
    HR
}
